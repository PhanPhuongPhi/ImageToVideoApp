package com.imagetovideo.app.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.RegisterRequest
import com.imagetovideo.app.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val name = binding.edtRegisterName.text.toString().trim()
            val email = binding.edtRegisterEmail.text.toString().trim()
            val password = binding.edtRegisterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(context, R.string.error_register_fill_all, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(email, password, name)
        }

        binding.txtBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun register(email: String, password: String, name: String) {
        val api = RetrofitClient.getApiService(requireContext())

        lifecycleScope.launch {
            try {
                val res = api.register(RegisterRequest(email, password, name))
                if (res.isSuccessful && res.body() != null) {
                    val expiresIn = res.body()!!.expiresIn
                    val bundle = Bundle().apply { 
                        putString("email", email) 
                        putLong("expires_in", expiresIn)
                    }
                    findNavController().navigate(
                        R.id.action_registerFragment_to_otpFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(context, R.string.register_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error_connection_general, Toast.LENGTH_SHORT)
                    .show()
                Log.e("Network", e.localizedMessage ?: "Unknown")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
