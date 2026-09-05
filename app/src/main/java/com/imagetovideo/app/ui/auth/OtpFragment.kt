package com.imagetovideo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.OtpVerifyRequest
import com.imagetovideo.app.data.model.ResendOtpRequest
import com.imagetovideo.app.databinding.FragmentOtpBinding
import com.imagetovideo.app.ui.main.MainActivity
import com.imagetovideo.app.utils.TokenManager
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String? = null
    private var expiresIn: Long = 300 // Default 5 mins
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")
        expiresIn = arguments?.getLong("expires_in", 300) ?: 300
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
                Toast.makeText(context, R.string.error_otp_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(otp)
        }

        binding.btnResendOtp.setOnClickListener {
            resendOtp()
        }

        startTimer(expiresIn)
    }

    private fun startTimer(seconds: Long) {
        timer?.cancel()
        binding.btnVerifyOtp.isEnabled = true
        binding.btnResendOtp.visibility = View.GONE
        
        timer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000
                val minutes = remainingSeconds / 60
                val secs = remainingSeconds % 60
                binding.txtOtpTimer.text = getString(R.string.otp_timer_format, minutes, secs)
            }

            override fun onFinish() {
                binding.txtOtpTimer.text = getString(R.string.otp_expired)
                binding.btnVerifyOtp.isEnabled = false
                binding.btnResendOtp.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun verifyOtp(otp: String) {
        if (email == null) return
        val api = RetrofitClient.getApiService(requireContext())
        val tokenManager = TokenManager(requireContext())

        lifecycleScope.launch {
            try {
                val res = api.verifyOtp(OtpVerifyRequest(email!!, otp))
                if (res.isSuccessful && res.body() != null) {
                    timer?.cancel()
                    tokenManager.saveToken(res.body()!!.accessToken)
                    tokenManager.saveUserEmail(email!!)
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, R.string.error_otp_wrong, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error_connection_general, Toast.LENGTH_SHORT)
                    .show()
                Log.e("Network", e.localizedMessage ?: "Unknown")
            }
        }
    }

    private fun resendOtp() {
        if (email == null) return
        val api = RetrofitClient.getApiService(requireContext())
        
        lifecycleScope.launch {
            try {
                val res = api.resendOtp(ResendOtpRequest(email!!))
                if (res.isSuccessful && res.body() != null) {
                    Toast.makeText(context, res.body()!!.message, Toast.LENGTH_SHORT).show()
                    startTimer(res.body()!!.expiresIn)
                } else {
                    Toast.makeText(context, R.string.error_connection_general, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error_connection_general, Toast.LENGTH_SHORT).show()
                Log.e("Network", e.localizedMessage ?: "Unknown")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
