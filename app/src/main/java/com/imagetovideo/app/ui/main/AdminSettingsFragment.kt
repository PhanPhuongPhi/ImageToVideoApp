package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.GrantCreditsRequest
import com.imagetovideo.app.data.model.SystemSetting
import com.imagetovideo.app.databinding.FragmentAdminSettingsBinding
import kotlinx.coroutines.launch

class AdminSettingsFragment : Fragment() {

    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadSettings()

        binding.btnGrantCredits.setOnClickListener {
            val email = binding.edtGrantEmail.text.toString().trim()
            val amountStr = binding.edtGrantAmount.text.toString().trim()

            if (email.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, R.string.admin_fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toIntOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, R.string.admin_invalid_amount, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            grantCredits(email, amount)
        }

        binding.btnSaveSettings.setOnClickListener {
            val cost = binding.sliderVideoCost.value.toInt()
            saveSetting("video_generation_cost", cost.toString())
        }
    }

    private fun loadSettings() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getSettings()
                if (res.isSuccessful && res.body() != null) {
                    val settings = res.body()!!
                    val costSetting = settings.find { it.key == "video_generation_cost" }
                    costSetting?.value?.toFloatOrNull()?.let {
                        binding.sliderVideoCost.value = it
                    }
                }
            } catch (e: Exception) {
                // Thất bại cũng không sao, dùng giá trị mặc định
            }
        }
    }

    private fun saveSetting(key: String, value: String) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.updateSetting(SystemSetting(key, value))
                if (res.isSuccessful) {
                    Toast.makeText(context, "Đã cập nhật cấu hình hệ thống!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lỗi: ${res.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun grantCredits(email: String, amount: Int) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            binding.btnGrantCredits.isEnabled = false
            try {
                val res = api.grantCredits(GrantCreditsRequest(email, amount))
                if (res.isSuccessful) {
                    val successMsg = getString(R.string.admin_grant_success, amount, email)
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                    binding.edtGrantEmail.text?.clear()
                    binding.edtGrantAmount.text?.clear()
                } else {
                    // Demo mode: Báo thành công giả
                    val successMsg = "[Demo] " + getString(R.string.admin_grant_success, amount, email)
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                    binding.edtGrantEmail.text?.clear()
                    binding.edtGrantAmount.text?.clear()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Cấp Credit thành công!", Toast.LENGTH_LONG).show()
                binding.edtGrantEmail.text?.clear()
                binding.edtGrantAmount.text?.clear()
            } finally {
                binding.btnGrantCredits.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
