package com.imagetovideo.app.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.imagetovideo.app.ui.main.AdminDashboardFragment
import com.imagetovideo.app.ui.main.AdminPromotionsFragment
import com.imagetovideo.app.ui.main.AdminSettingsFragment
import com.imagetovideo.app.ui.main.AdminUsersFragment
import com.imagetovideo.app.ui.main.AdminVideosFragment

class AdminPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminDashboardFragment()
            1 -> AdminUsersFragment()
            2 -> AdminVideosFragment()
            3 -> AdminPromotionsFragment()
            4 -> AdminSettingsFragment()
            else -> AdminDashboardFragment()
        }
    }
}
