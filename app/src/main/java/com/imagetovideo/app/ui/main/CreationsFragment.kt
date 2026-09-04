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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.VideoItem
import com.imagetovideo.app.databinding.DialogVideoPlayerBinding
import com.imagetovideo.app.databinding.FragmentCreationsBinding
import com.imagetovideo.app.ui.adapter.VideoAdapter
import kotlinx.coroutines.launch

class CreationsFragment : Fragment() {

    private var _binding: FragmentCreationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VideoAdapter { video ->
            showVideoPlayer(video)
        }
        binding.rvCreations.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadHistory()
        }

        loadHistory()
    }

    private fun loadHistory() {
        val api = RetrofitClient.getApiService(requireContext())

        lifecycleScope.launch {
            _binding?.swipeRefresh?.isRefreshing = true
            try {
                val res = api.getVideoHistory(page = 1, limit = 50)
                if (res.isSuccessful && res.body() != null) {
                    adapter.submitList(res.body()!!.items)
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, R.string.msg_load_creations_error, Toast.LENGTH_SHORT).show()
                }
            } finally {
                _binding?.swipeRefresh?.isRefreshing = false
            }
        }
    }

    private fun showVideoPlayer(video: VideoItem) {
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
        
        dialogBinding.btnClosePlayer.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.setOnDismissListener {
            player.release()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
