package com.imagetovideo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.LoginRequest
import com.imagetovideo.app.databinding.FragmentLoginBinding
import com.imagetovideo.app.ui.main.MainActivity
import com.imagetovideo.app.utils.TokenManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(email, password)
        }

        binding.txtGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login(email: String, password: String) {
        val api = RetrofitClient.getApiService(requireContext())
        val tokenManager = TokenManager(requireContext())

        lifecycleScope.launch {
            try {
                val res = api.login(LoginRequest(email, password))
                if (res.isSuccessful && res.body() != null) {
                    val token = res.body()!!.accessToken
                    tokenManager.saveToken(token)
                    tokenManager.saveUserEmail(email)

                    // Lấy profile để lấy role thực từ API
                    val profileRes = api.getMe()
                    if (profileRes.isSuccessful && profileRes.body() != null) {
                        val userProfile = profileRes.body()!!
                        tokenManager.saveUserRole(userProfile.role)
                        Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show()
                    }

                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    val errorMsg = res.errorBody()?.string() ?: getString(R.string.login_failed)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.error_connection, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
