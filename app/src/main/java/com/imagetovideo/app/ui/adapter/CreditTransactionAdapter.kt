package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.imagetovideo.app.data.model.CreditTransaction
import com.imagetovideo.app.databinding.ItemCreditTransactionBinding

class CreditTransactionAdapter(private var items: List<CreditTransaction>) :
    RecyclerView.Adapter<CreditTransactionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCreditTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCreditTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtReason.text = item.reason
        holder.binding.txtDate.text = item.createdAt

        if (!item.prompt.isNullOrEmpty()) {
            holder.binding.txtPrompt.visibility = android.view.View.VISIBLE
            holder.binding.txtPrompt.text = holder.itemView.context.getString(
                com.imagetovideo.app.R.string.transaction_prompt_label,
                item.prompt
            )
        } else {
            holder.binding.txtPrompt.visibility = android.view.View.GONE
        }

        if (item.type == "PLUS") {
            holder.binding.txtAmount.text = holder.itemView.context.getString(
                com.imagetovideo.app.R.string.amount_plus,
                item.amount
            )
            holder.binding.txtAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_green_dark
                )
            )
            holder.binding.imgType.setImageResource(android.R.drawable.ic_input_add)
        } else {
            holder.binding.txtAmount.text = holder.itemView.context.getString(
                com.imagetovideo.app.R.string.amount_minus,
                item.amount
            )
            holder.binding.txtAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_red_dark
                )
            )
            holder.binding.imgType.setImageResource(android.R.drawable.ic_delete)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CreditTransaction>) {
        items = newItems
        notifyDataSetChanged()
    }
}
