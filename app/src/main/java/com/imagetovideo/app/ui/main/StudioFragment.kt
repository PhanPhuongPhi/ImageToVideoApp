package com.imagetovideo.app.ui.main

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.databinding.FragmentStudioBinding
import com.imagetovideo.app.utils.TokenManager
import com.imagetovideo.app.utils.NotificationHelper
import com.imagetovideo.app.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class StudioFragment : Fragment() {

    private var _binding: FragmentStudioBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var exoPlayer: ExoPlayer? = null
    private var currentVideoUrl: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreview.setImageURI(it)
            binding.imgPreview.visibility = View.VISIBLE
            binding.layoutUploadPlaceholder.visibility = View.GONE
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Thông báo bị từ chối. Bạn sẽ không nhận được tin nhắn khi video hoàn thành.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudioBinding.inflate(inflater, container, false)
        NotificationHelper.createNotificationChannel(requireContext())
        checkNotificationPermission()
        return binding.root
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnGenerate.setOnClickListener {
            val prompt = binding.edtPrompt.text.toString().trim()
            if (selectedImageUri == null) {
                Toast.makeText(context, "Vui lòng chọn 1 bức ảnh!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (prompt.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập Prompt!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ratio = when (binding.toggleRatio.checkedButtonId) {
                R.id.btnRatio169 -> "16:9"
                R.id.btnRatio916 -> "9:16"
                R.id.btnRatio11 -> "1:1"
                else -> "16:9"
            }

            generateVideo(selectedImageUri!!, prompt, ratio)
        }

        binding.toggleRatio.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                binding.cardSelectImage.post {
                    updatePreviewRatio(checkedId)
                }
            }
        }

        // Khởi tạo tỉ lệ mặc định
        binding.cardSelectImage.post {
            updatePreviewRatio(binding.toggleRatio.checkedButtonId)
        }

        binding.btnSaveToApp.setOnClickListener {
            currentVideoUrl?.let { url ->
                saveVideoToApp(url)
            }
        }

        binding.btnDownloadToDevice.setOnClickListener {
            currentVideoUrl?.let { url ->
                downloadVideoToDevice(url)
            }
        }

        binding.btnShareVideo.setOnClickListener {
            currentVideoUrl?.let { url ->
                shareVideo(url)
            }
        }
    }

    private fun updatePreviewRatio(checkedId: Int) {
        val params = binding.cardSelectImage.layoutParams
        val width = binding.cardSelectImage.width
        if (width <= 0) return

        when (checkedId) {
            R.id.btnRatio169 -> params.height = (width * 9 / 16)
            R.id.btnRatio916 -> params.height = (width * 16 / 9)
            R.id.btnRatio11 -> params.height = width
        }
        binding.cardSelectImage.layoutParams = params
    }

    private fun shareVideo(videoUrl: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "Xem video AI của tôi được tạo từ Image To Video App: $videoUrl")
        }
        startActivity(android.content.Intent.createChooser(intent, "Chia sẻ video"))
    }

    private fun generateVideo(imageUri: Uri, promptText: String, ratio: String) {
        val context = requireContext()
        val api = RetrofitClient.getApiService(context)

        lifecycleScope.launch {
            _binding?.btnGenerate?.isEnabled = false

            try {
                // 0. Kiểm tra số dư Credit trước khi thực hiện
                val meRes = api.getMe()
                if (meRes.isSuccessful && meRes.body() != null) {
                    val balance = meRes.body()!!.creditBalance
                    if (balance < 1) {
                        Toast.makeText(context, R.string.credit_insufficient, Toast.LENGTH_LONG).show()
                        _binding?.btnGenerate?.isEnabled = true
                        return@launch
                    }
                }

                _binding?.layoutGeneratingStatus?.visibility = View.VISIBLE
                _binding?.progressGenerating?.progress = 0
                _binding?.layoutVideoResult?.visibility = View.GONE

                // Chuyển URI thành File tạm thời
                val file = uriToFile(imageUri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val promptBody = promptText.toRequestBody("text/plain".toMediaTypeOrNull())
                val ratioBody = ratio.toRequestBody("text/plain".toMediaTypeOrNull())

                // 1. Gửi request sinh video
                val res = api.generateVideo(imagePart, promptBody, ratioBody)
                if (res.isSuccessful && res.body() != null) {
                    _binding?.txtSuccessMessage?.visibility = View.GONE
                    // Cập nhật Credit ngay khi bắt đầu (trừ tạm)
                    (activity as? MainActivity)?.fetchCredits()
                    
                    val jobId = res.body()!!.jobId

                    // 2. Polling API kiểm tra trạng thái mỗi 5 giây
                    var elapsed = 0
                    var isCompleted = false
                    var videoUrl = ""

                    while (!isCompleted) {
                        delay(5000)
                        elapsed += 5
                        
                        // Cập nhật Progress Bar giả lập (tăng dần đến 95%)
                        val simulatedProgress = (elapsed * 2).coerceAtMost(95)
                        _binding?.progressGenerating?.setProgress(simulatedProgress, true)
                        _binding?.txtStatusTimer?.text = "Đang xử lý tạo video AI... (${elapsed}s - $simulatedProgress%)"

                        val statusRes = api.getVideoStatus(jobId)
                        if (statusRes.isSuccessful && statusRes.body() != null) {
                            val statusBody = statusRes.body()!!
                            val currentStatus = statusBody.status?.uppercase()
                            
                            if (currentStatus == "COMPLETED" && !statusBody.videoUrl.isNullOrEmpty()) {
                                isCompleted = true
                                _binding?.progressGenerating?.setProgress(100, true)
                                _binding?.txtStatusTimer?.text = "Hoàn tất!"
                                _binding?.txtSuccessMessage?.visibility = View.VISIBLE
                                delay(500)
                                videoUrl = RetrofitClient.resolveMediaUrl(statusBody.videoUrl)
                                
                                // Gửi thông báo hệ thống
                                NotificationHelper.showVideoCompletedNotification(context, promptText)
                            } else if (currentStatus == "FAILED" || currentStatus == "ERROR") {
                                throw Exception(statusBody.errorMessage ?: "Tạo video thất bại")
                            }
                        } else if (statusRes.code() == 404) {
                            throw Exception("Không tìm thấy thông tin video trên Server. Vui lòng thử lại!")
                        }
                    }

                    // 3. Hiển thị ExoPlayer kết quả
                    _binding?.layoutGeneratingStatus?.visibility = View.GONE
                    currentVideoUrl = videoUrl
                    playVideo(videoUrl)

                    // 4. Tự động lưu vào App
                    saveVideoToApp(videoUrl, isAutoSave = true)

                    // Cập nhật lại số dư Credit trên Toolbar
                    (activity as? MainActivity)?.fetchCredits()
                } else {
                    Toast.makeText(context, "Không thể khởi tạo tiến trình tạo video!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    _binding?.layoutGeneratingStatus?.visibility = View.GONE
                }
            } finally {
                _binding?.btnGenerate?.isEnabled = true
            }
        }
    }

    private fun playVideo(url: String) {
        binding.layoutVideoResult.visibility = View.VISIBLE
        binding.btnSaveToApp.isEnabled = true
        binding.btnSaveToApp.text = getString(R.string.studio_btn_save)
        binding.btnDownloadToDevice.isEnabled = true

        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
        binding.playerView.player = exoPlayer
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_img_", ".jpg", requireContext().cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    private fun saveVideoToApp(videoUrl: String, isAutoSave: Boolean = false) {
        val context = requireContext()
        if (!isAutoSave) {
            binding.btnSaveToApp.isEnabled = false
            binding.btnSaveToApp.text = "Đang lưu..."
        }

        lifecycleScope.launch {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder().url(videoUrl).build()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        val fileName = "video_${System.currentTimeMillis()}.mp4"
                        val file = File(context.filesDir, fileName)
                        val fos = FileOutputStream(file)
                        fos.write(bytes)
                        fos.close()

                        if (!isAutoSave) {
                            Toast.makeText(context, R.string.studio_save_success, Toast.LENGTH_LONG).show()
                            binding.btnSaveToApp.text = "Đã lưu vào App"
                        } else {
                            Toast.makeText(context, "Video đã được tự động lưu vào App!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    throw Exception("Tải video thất bại")
                }
            } catch (e: Exception) {
                if (!isAutoSave) {
                    Toast.makeText(context, "${getString(R.string.studio_save_error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSaveToApp.isEnabled = true
                    binding.btnSaveToApp.text = getString(R.string.studio_btn_save)
                }
            }
        }
    }

    private fun downloadVideoToDevice(videoUrl: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(videoUrl))
                .setTitle("AI Video Generation")
                .setDescription("Đang tải video từ Image To Video App")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "AI_Video_${System.currentTimeMillis()}.mp4")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            Toast.makeText(requireContext(), "Đã bắt đầu tải video về máy!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi tải về: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
        _binding = null
    }
}
