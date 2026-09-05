package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imagetovideo.app.data.model.Promotion
import com.imagetovideo.app.databinding.ItemAdminPromotionBinding

class AdminPromotionAdapter(private var promos: List<Promotion>) :
    RecyclerView.Adapter<AdminPromotionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminPromotionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPromotionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val promo = promos[position]
        val context = holder.itemView.context
        holder.binding.txtPromoName.text = promo.name
        holder.binding.txtPromoReward.text = context.getString(com.imagetovideo.app.R.string.admin_promo_reward_formatted, promo.rewardCredits)
        holder.binding.txtPromoDuration.text = context.getString(com.imagetovideo.app.R.string.date_range_formatted, promo.startDate, promo.endDate)
        holder.binding.txtPromoStatus.text = if (promo.isActive) {
            context.getString(com.imagetovideo.app.R.string.admin_promo_status_active)
        } else {
            context.getString(com.imagetovideo.app.R.string.admin_promo_status_inactive)
        }
    }

    override fun getItemCount() = promos.size

    fun updateData(newPromos: List<Promotion>) {
        promos = newPromos
        notifyDataSetChanged()
    }
}
