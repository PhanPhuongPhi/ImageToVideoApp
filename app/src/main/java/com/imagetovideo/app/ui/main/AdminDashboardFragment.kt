package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getAdminStats()
                if (res.isSuccessful && res.body() != null) {
                    val stats = res.body()!!
                    updateUI(stats.totalUsers, stats.newUsersToday, stats.totalVideosSuccess, stats.totalVideosFailed, stats.totalRevenue, stats.activePromotionsCount)
                } else {
                    showMockStats()
                }
            } catch (e: Exception) {
                showMockStats()
            }
        }
    }

    private fun showMockStats() {
        updateUI(150, 12, 1240, 5, 2500000.0, 3)
    }

    private fun updateUI(users: Int, newUsers: Int, videoOk: Int, videoFail: Int, rev: Double, promos: Int) {
        binding.txtStatTotalUsers.text = users.toString()
        binding.txtStatNewUsers.text = newUsers.toString()
        binding.txtStatVideosSuccess.text = videoOk.toString()
        binding.txtStatVideosFailed.text = videoFail.toString()
        binding.txtStatRevenue.text = String.format("%,.0f", rev)
        binding.txtStatPromotions.text = promos.toString()

        setupPieChart(videoOk, videoFail)
    }

    private fun setupPieChart(success: Int, failed: Int) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(success.toFloat(), getString(R.string.chart_success)))
        entries.add(PieEntry(failed.toFloat(), getString(R.string.chart_failed)))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            "#10B981".toColorInt(), // emerald_accent
            "#EF4444".toColorInt()  // red_accent
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.WHITE

        val data = PieData(dataSet)
        binding.pieChartVideos.data = data
        binding.pieChartVideos.description.isEnabled = false
        binding.pieChartVideos.centerText = getString(R.string.chart_video_rate)
        binding.pieChartVideos.setCenterTextColor(android.graphics.Color.WHITE)
        binding.pieChartVideos.setHoleColor(android.graphics.Color.TRANSPARENT)
        binding.pieChartVideos.legend.isEnabled = false
        binding.pieChartVideos.animateY(1000)
        binding.pieChartVideos.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
