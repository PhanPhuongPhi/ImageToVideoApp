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
import com.imagetovideo.app.databinding.FragmentCreditHistoryBinding
import com.imagetovideo.app.ui.adapter.CreditTransactionAdapter
import kotlinx.coroutines.launch

class CreditHistoryFragment : Fragment() {

    private var _binding: FragmentCreditHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CreditTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CreditTransactionAdapter(emptyList())
        binding.rvCreditHistory.layoutManager = LinearLayoutManager(context)
        binding.rvCreditHistory.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getCreditHistory()
                if (res.isSuccessful && res.body() != null) {
                    val history = res.body()!!.items
                    if (history.isEmpty()) {
                        binding.txtEmpty.visibility = View.VISIBLE
                        binding.rvCreditHistory.visibility = View.GONE
                    } else {
                        binding.txtEmpty.visibility = View.GONE
                        binding.rvCreditHistory.visibility = View.VISIBLE
                        adapter.updateData(history)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi tải lịch sử: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
