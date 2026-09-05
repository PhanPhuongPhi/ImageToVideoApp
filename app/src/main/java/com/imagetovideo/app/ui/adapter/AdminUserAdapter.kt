package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imagetovideo.app.data.model.UserProfile
import com.imagetovideo.app.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private var users: List<UserProfile>,
    private val onLockToggle: (UserProfile, Boolean) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        
        if (!user.fullName.isNullOrEmpty()) {
            holder.binding.txtUserName.text = user.fullName
            holder.binding.txtUserName.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.txtUserName.visibility = android.view.View.GONE
        }
        
        holder.binding.txtUserEmail.text = user.email
        holder.binding.txtUserRoleStatus.text = holder.itemView.context.getString(
            com.imagetovideo.app.R.string.admin_user_role_balance,
            user.role,
            user.creditBalance
        )
        
        holder.binding.switchLockUser.setOnCheckedChangeListener(null)
        holder.binding.switchLockUser.isChecked = user.isLocked
        
        holder.binding.switchLockUser.setOnCheckedChangeListener { _, isChecked ->
            onLockToggle(user, isChecked)
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserProfile>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
