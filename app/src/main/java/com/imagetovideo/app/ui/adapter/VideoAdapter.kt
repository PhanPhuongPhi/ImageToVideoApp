package com.imagetovideo.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imagetovideo.app.R
import com.imagetovideo.app.data.model.VideoItem
import com.imagetovideo.app.databinding.ItemVideoBinding

class VideoAdapter(private val onItemClick: (VideoItem) -> Unit) :
    ListAdapter<VideoItem, VideoAdapter.VideoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class VideoViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VideoItem) {
            binding.txtItemPrompt.text = item.prompt
            binding.txtItemDate.text = item.createdAt.take(10) // YYYY-MM-DD

            Glide.with(binding.imgVideoThumbnail.context)
                .load(item.thumbnailUrl ?: item.videoUrl)
                .placeholder(R.drawable.ic_studio)
                .centerCrop()
                .into(binding.imgVideoThumbnail)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }
}
