package com.imagetovideo.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.UpdateProfileRequest
import com.imagetovideo.app.data.model.UserRole
import com.imagetovideo.app.databinding.DialogEditProfileBinding
import com.imagetovideo.app.databinding.FragmentProfileBinding
import com.imagetovideo.app.ui.auth.AuthActivity
import com.imagetovideo.app.utils.TokenManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext())
        binding.txtProfileEmail.text = tokenManager.getUserEmailSync()
        binding.txtProfileRole.text = getString(R.string.role_label, tokenManager.getUserRoleSync())

        loadProfile()

        if (tokenManager.getUserRoleSync() == UserRole.ADMIN) {
            binding.btnAdminPanel.visibility = View.VISIBLE
        }

        binding.btnAdminPanel.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_adminFragment)
        }

        binding.btnCreditHistory.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_creditHistoryFragment)
        }

        binding.btnBuyCredits.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_creditPackagesFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.clear()
                Toast.makeText(context, R.string.error_logout_success, Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root).create()

        dialogBinding.btnUpdateProfile.setOnClickListener {
            val name = dialogBinding.edtProfileName.text.toString().trim()
            val password = dialogBinding.edtProfilePassword.text.toString().trim()

            if (name.isEmpty() && password.isEmpty()) {
                Toast.makeText(context, R.string.error_update_profile_empty, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val request = UpdateProfileRequest(
                name = name.ifEmpty { null },
                password = password.ifEmpty { null })

            updateProfile(request, dialog)
        }

        dialog.show()
    }

    private fun updateProfile(request: UpdateProfileRequest, dialog: android.app.Dialog) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.updateProfile(request)
                if (res.isSuccessful) {
                    Toast.makeText(
                        context, R.string.error_update_profile_success, Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    loadProfile()
                } else {
                    Toast.makeText(
                        context, R.string.error_connection, Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Profile", res.message())
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context, R.string.error_connection, Toast.LENGTH_SHORT
                ).show()
                Log.e("Profile", e.localizedMessage ?: "Unknown")
            }
        }
    }

    private fun loadProfile() {
        val api = RetrofitClient.getApiService(requireContext())
        val tokenManager = TokenManager(requireContext())

        lifecycleScope.launch {
            try {
                val meRes = api.getMe()
                if (meRes.isSuccessful && meRes.body() != null) {
                    val me = meRes.body()!!
                    _binding?.txtProfileEmail?.text =
                        if (!me.fullName.isNullOrEmpty()) "${me.fullName}\n${me.email}" else me.email
                    _binding?.txtProfileRole?.text = getString(R.string.role_label, me.role)
                    _binding?.txtProfileCredit?.text =
                        getString(R.string.credits_balance, me.creditBalance.toString())

                    // Cập nhật lại role vào storage
                    tokenManager.saveUserRole(me.role)
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(
                        context,
                        getString(R.string.error_load_profile, e.localizedMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
