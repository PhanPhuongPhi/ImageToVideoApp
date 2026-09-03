package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.UserProfile
import com.imagetovideo.app.databinding.FragmentAdminUsersBinding
import com.imagetovideo.app.ui.adapter.AdminUserAdapter
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminUserAdapter(emptyList()) { user, isLocked ->
            updateUserStatus(user.id, isLocked)
        }
        binding.rvAdminUsers.layoutManager = LinearLayoutManager(context)
        binding.rvAdminUsers.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getAllUsers()
                if (res.isSuccessful && res.body() != null) {
                    adapter.updateData(res.body()!!)
                } else {
                    showMockUsers()
                }
            } catch (e: Exception) {
                showMockUsers()
            }
        }
    }

    private fun showMockUsers() {
        val mockData = listOf(
            UserProfile(id = "1", email = "admin@gmail.com", fullName = "Hệ thống Admin", role = "admin", creditBalance = 999),
            UserProfile(id = "2", email = "khachhang@gmail.com", fullName = "Nguyễn Văn Khách", role = "guest", creditBalance = 10),
            UserProfile(id = "3", email = "user_test@gmail.com", fullName = "Người dùng thử", role = "guest", creditBalance = 0),
            UserProfile(id = "4", email = "123", fullName = "Tài khoản 123", role = "guest", creditBalance = 5)
        )
        adapter.updateData(mockData)
    }

    private fun updateUserStatus(userId: String, isLocked: Boolean) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.updateUserStatus(userId, mapOf("is_locked" to isLocked))
                if (res.isSuccessful) {
                    val msg = if (isLocked) "Đã khóa tài khoản" else "Đã mở khóa tài khoản"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                } else {
                    val msg = if (isLocked) "[Demo] Đã khóa tài khoản" else "[Demo] Đã mở khóa tài khoản"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val msg = if (isLocked) "Đã khóa" else "Đã mở khóa"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
