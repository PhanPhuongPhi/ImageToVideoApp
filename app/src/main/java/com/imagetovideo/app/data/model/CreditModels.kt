package com.imagetovideo.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Lịch sử giao dịch Credit (Cộng/Trừ)
 */
data class CreditTransaction(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("transaction_type") val type: String, // "PLUS" hoặc "MINUS"
    @SerializedName("reason") val reason: String,
    @SerializedName("created_at") val createdAt: String
)

/**
 * Gói mua thêm Credit
 */
data class CreditPackage(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("credits") val credits: Int,
    @SerializedName("description") val description: String? = null
)

/**
 * Chương trình khuyến mãi
 */
data class Promotion(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("reward_credits") val rewardCredits: Int,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class TransactionHistoryResponse(
    val items: List<CreditTransaction>,
    val total: Int
)

data class CreditPackagesResponse(
    val items: List<CreditPackage>
)

/**
 * Đăng ký tham gia khuyến mãi (Lưu vết user đã nhận thưởng)
 */
data class PromotionRegistration(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("promotion_id") val promotionId: String,
    @SerializedName("registered_at") val registeredAt: String
)

/**
 * Cấu hình hệ thống (Ví dụ: Chi phí tạo video)
 */
data class SystemSetting(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String,
    @SerializedName("description") val description: String? = null
)

/**
 * Thống kê cho Admin Dashboard
 */
data class AdminDashboardStats(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("new_users_today") val newUsersToday: Int,
    @SerializedName("total_videos_success") val totalVideosSuccess: Int,
    @SerializedName("total_videos_failed") val totalVideosFailed: Int,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("active_promotions_count") val activePromotionsCount: Int
)
