package com.imagetovideo.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imagetovideo.app.R
import com.imagetovideo.app.data.api.RetrofitClient
import com.imagetovideo.app.data.model.Promotion
import com.imagetovideo.app.databinding.DialogCreatePromotionBinding
import com.imagetovideo.app.databinding.FragmentAdminPromotionsBinding
import com.imagetovideo.app.ui.adapter.AdminPromotionAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminPromotionsFragment : Fragment() {

    private var _binding: FragmentAdminPromotionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminPromotionAdapter
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var startDateStr: String? = null
    private var endDateStr: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPromotionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminPromotionAdapter(emptyList())
        binding.rvAdminPromotions.layoutManager = LinearLayoutManager(context)
        binding.rvAdminPromotions.adapter = adapter

        binding.fabAddPromotion.setOnClickListener {
            showCreatePromotionDialog()
        }

        loadPromotions()
    }

    private fun showCreatePromotionDialog() {
        val dialogBinding = DialogCreatePromotionBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root).create()

        dialogBinding.btnStartDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker().setTitleText(R.string.picker_start_date)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                startDateStr = dateFormatter.format(Date(selection))
                dialogBinding.btnStartDate.text = startDateStr
            }
            datePicker.show(childFragmentManager, "START_DATE_PICKER")
        }

        dialogBinding.btnEndDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker().setTitleText(R.string.picker_end_date)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                endDateStr = dateFormatter.format(Date(selection))
                dialogBinding.btnEndDate.text = endDateStr
            }
            datePicker.show(childFragmentManager, "END_DATE_PICKER")
        }

        dialogBinding.btnCreate.setOnClickListener {
            val name = dialogBinding.edtPromoName.text.toString().trim()
            val rewardStr = dialogBinding.edtPromoReward.text.toString().trim()

            if (name.isEmpty() || rewardStr.isEmpty() || startDateStr == null || endDateStr == null) {
                Toast.makeText(context, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reward = rewardStr.toIntOrNull() ?: 0
            val promotion = Promotion(
                id = "", // Backend sẽ sinh ID
                name = name,
                rewardCredits = reward,
                startDate = startDateStr!!,
                endDate = endDateStr!!,
                isActive = true
            )

            createPromotion(promotion, dialog)
        }

        dialog.show()
    }

    private fun createPromotion(promotion: Promotion, dialog: android.app.Dialog) {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.createPromotion(promotion)
                if (res.isSuccessful) {
                    Toast.makeText(context, R.string.admin_promo_success, Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    loadPromotions() // Reload list
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_generic, res.message()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    getString(R.string.error_connection, e.localizedMessage),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadPromotions() {
        val api = RetrofitClient.getApiService(requireContext())
        lifecycleScope.launch {
            try {
                val res = api.getActivePromotions()
                if (res.isSuccessful && res.body() != null) {
                    adapter.updateData(res.body()!!)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context, getString(R.string.error_load_promotions, e.localizedMessage), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
