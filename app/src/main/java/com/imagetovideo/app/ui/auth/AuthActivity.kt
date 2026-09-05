package com.imagetovideo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.imagetovideo.app.R
import com.imagetovideo.app.databinding.ActivityAuthBinding
import com.imagetovideo.app.ui.main.MainActivity
import com.imagetovideo.app.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tokenManager = TokenManager(this)
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (!token.isNullOrEmpty()) {
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            } else {
                setupUI()
            }
        }
    }

    private fun setupUI() {
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.navHostAuth)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
