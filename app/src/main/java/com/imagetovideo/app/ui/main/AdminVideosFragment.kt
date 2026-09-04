package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.AdminVideoItem
import com.imagetovideo.app.databinding.DialogVideoPlayerBinding
import com.imagetovideo.app.databinding.FragmentAdminVideosBinding
import com.imagetovideo.app.ui.adapter.AdminVideoAdapter
import kotlinx.coroutines.launch

class AdminVideosFragment : Fragment() {

    private var _binding: FragmentAdminVideosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminVideoAdapter(
            emptyList(),
            onVideoClick = { video -> showVideoPlayer(video) },
            onDeleteClick = { video -> confirmDelete(video) }
        )
        binding.rvAdminVideos.layoutManager = LinearLayoutManager(context)
        binding.rvAdminVideos.adapter = adapter

        binding.swipeRefreshAdminVideos.setOnRefreshListener {
            loadVideos()
        }

        loadVideos()
    }

    private fun loadVideos() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            _binding?.swipeRefreshAdminVideos?.isRefreshing = true
            try {
                val res = api.getAllAdminVideos()
                if (res.isSuccessful && res.body() != null) {
                    adapter.updateData(res.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    getString(R.string.error_load_video, e.localizedMessage),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _binding?.swipeRefreshAdminVideos?.isRefreshing = false
            }
        }
    }

    private fun showVideoPlayer(video: AdminVideoItem) {
        val dialogBinding = DialogVideoPlayerBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.txtPlayerPrompt.text = video.prompt
        val fullVideoUrl = RetrofitClient.resolveMediaUrl(video.videoUrl)

        val player = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(MediaItem.fromUri(fullVideoUrl))
            prepare()
            playWhenReady = true
        }

        dialogBinding.dialogPlayerView.player = player
        dialogBinding.btnClosePlayer.setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { player.release() }
        dialog.show()
    }

    private fun confirmDelete(video: AdminVideoItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_video_title)
            .setMessage(R.string.dialog_delete_video_msg)
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                deleteVideo(video.id)
            }
            .show()
    }

    private fun deleteVideo(videoId: String) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.adminDeleteVideo(videoId)
                if (res.isSuccessful) {
                    Toast.makeText(context, R.string.msg_video_deleted, Toast.LENGTH_SHORT).show()
                    loadVideos()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.msg_video_delete_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
