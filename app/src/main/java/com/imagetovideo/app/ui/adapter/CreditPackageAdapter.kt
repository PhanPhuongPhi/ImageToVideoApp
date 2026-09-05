package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imagetovideo.app.data.model.CreditPackage
import com.imagetovideo.app.databinding.ItemCreditPackageBinding

class CreditPackageAdapter(
    private var items: List<CreditPackage>,
    private val onBuyClick: (CreditPackage) -> Unit
) : RecyclerView.Adapter<CreditPackageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCreditPackageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCreditPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        holder.binding.packageName.text = item.name
        holder.binding.packageCredits.text = context.getString(com.imagetovideo.app.R.string.credits_balance, item.credits.toString())
        holder.binding.packagePrice.text = "${String.format("%,.0f", item.price)} ${context.getString(com.imagetovideo.app.R.string.currency_vnd)}"
        
        holder.binding.btnBuy.setOnClickListener {
            onBuyClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CreditPackage>) {
        items = newItems
        notifyDataSetChanged()
    }
}
