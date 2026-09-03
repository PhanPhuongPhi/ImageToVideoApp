package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imagetovideo.app.data.model.AdminVideoItem
import com.imagetovideo.app.databinding.ItemAdminVideoBinding

class AdminVideoAdapter(
    private var videos: List<AdminVideoItem>,
    private val onVideoClick: (AdminVideoItem) -> Unit,
    private val onDeleteClick: (AdminVideoItem) -> Unit
) : RecyclerView.Adapter<AdminVideoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = videos[position]
        holder.binding.txtAdminVideoUser.text = "User: ${item.userEmail}"
        holder.binding.txtAdminVideoPrompt.text = item.prompt
        holder.binding.txtAdminVideoStatus.text = "Status: ${item.status}"
        
        holder.binding.root.setOnClickListener { onVideoClick(item) }
        holder.binding.btnAdminDeleteVideo.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = videos.size

    fun updateData(newVideos: List<AdminVideoItem>) {
        videos = newVideos
        notifyDataSetChanged()
    }
}
