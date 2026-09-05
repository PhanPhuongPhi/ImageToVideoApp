package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.databinding.FragmentCreditPackagesBinding
import com.imagetovideo.app.ui.adapter.CreditPackageAdapter
import kotlinx.coroutines.launch

class CreditPackagesFragment : Fragment() {

    private var _binding: FragmentCreditPackagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CreditPackageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditPackagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CreditPackageAdapter(emptyList()) { pkg ->
            purchasePackage(pkg.id)
        }
        binding.rvPackages.layoutManager = LinearLayoutManager(context)
        binding.rvPackages.adapter = adapter

        loadPackages()
    }

    private fun loadPackages() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getCreditPackages()
                if (res.isSuccessful && res.body() != null) {
                    adapter.updateData(res.body()!!.items)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context, getString(R.string.error_load_packages, e.localizedMessage), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun purchasePackage(packageId: String) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.purchaseCredit(packageId)
                if (res.isSuccessful) {
                    Toast.makeText(context, R.string.credit_purchase_success, Toast.LENGTH_LONG)
                        .show()
                    (activity as? MainActivity)?.fetchCredits()
                } else {
                    Toast.makeText(
                        context, getString(R.string.credit_error_purchase), Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    getString(R.string.error_generic, e.localizedMessage),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
