package com.imagetovideo.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "creations") {
            navController.navigate(R.id.creationsFragment)
        }

        fetchCredits()
    }

    fun fetchCredits() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApiService(this@MainActivity)
                val res = api.getCredits()
                if (res.isSuccessful && res.body() != null) {
                    val balance = res.body()!!.creditBalance.toString()
                    binding.txtCreditBalance.text = getString(R.string.credits_balance, balance)
                }
            } catch (_: Exception) {}
        }
    }
}
