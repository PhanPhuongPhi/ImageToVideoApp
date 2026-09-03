package com.imagetovideo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.OtpVerifyRequest
import com.imagetovideo.app.databinding.FragmentOtpBinding
import com.imagetovideo.app.ui.main.MainActivity
import com.imagetovideo.app.utils.TokenManager
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.edtOtpCode.text.toString().trim()
            if (otp.length != 6) {
                Toast.makeText(context, "Mã OTP gồm 6 chữ số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(otp)
        }
    }

    private fun verifyOtp(otp: String) {
        if (email == null) return
        val api = RetrofitClient.getApiService(requireContext())
        val tokenManager = TokenManager(requireContext())

        lifecycleScope.launch {
            try {
                val res = api.verifyOtp(OtpVerifyRequest(email!!, otp))
                if (res.isSuccessful && res.body() != null) {
                    tokenManager.saveToken(res.body()!!.accessToken)
                    tokenManager.saveUserEmail(email!!)
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "Mã OTP không đúng!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
