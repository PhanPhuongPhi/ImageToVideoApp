package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.databinding.FragmentExploreBinding
import com.imagetovideo.app.ui.adapter.VideoAdapter
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VideoAdapter { video ->
            Toast.makeText(
                context,
                getString(R.string.explore_view_sample, video.prompt),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvExplore.adapter = adapter

        binding.swipeRefreshExplore.setOnRefreshListener {
            loadExploreFeed()
        }

        loadExploreFeed()
    }

    private fun loadExploreFeed() {
        val api = RetrofitClient.getApiService(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            _binding?.swipeRefreshExplore?.isRefreshing = true
            try {
                val res = api.getVideoHistory(page = 1, limit = 20)
                if (res.isSuccessful && res.body() != null) {
                    adapter.submitList(res.body()!!.items)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(context, R.string.explore_load_error, Toast.LENGTH_SHORT).show()
                    Log.e("Explore", e.localizedMessage ?: "Unknown")
                }
            } finally {
                _binding?.swipeRefreshExplore?.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
