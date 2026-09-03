package com.imagetovideo.app.data.model

import com.google.gson.annotations.SerializedName

data class VideoJob(
    @SerializedName("job_id", alternate = ["task_id"]) val jobId: String,
    val status: String? = null, // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("error_message") val errorMessage: String? = null
)

data class VideoHistory(
    val items: List<VideoItem>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class VideoItem(
    @SerializedName("id") val id: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("created_at") val createdAt: String
)

data class AdminVideoItem(
    @SerializedName("id") val id: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("user_email") val userEmail: String
)
