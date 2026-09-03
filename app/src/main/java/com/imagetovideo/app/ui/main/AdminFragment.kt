package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.R
import com.imagetovideo.app.databinding.FragmentAdminBinding
import com.imagetovideo.app.ui.adapter.AdminPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AdminPagerAdapter(this)
        binding.adminViewPager.adapter = adapter

        TabLayoutMediator(binding.adminTabLayout, binding.adminViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.admin_tab_dashboard)
                1 -> getString(R.string.admin_tab_users)
                2 -> getString(R.string.admin_tab_videos)
                3 -> getString(R.string.admin_tab_promotions)
                4 -> getString(R.string.admin_tab_settings)
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
