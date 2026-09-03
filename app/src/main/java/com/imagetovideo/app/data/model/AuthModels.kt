package com.imagetovideo.app.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class OtpVerifyRequest(
    val email: String,
    val otp: String
)

data class UserProfile(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String? = null,
    val role: String,
    @SerializedName("credit_balance") val creditBalance: Int,
    @SerializedName("is_locked") val isLocked: Boolean = false
)

data class GrantCreditsRequest(
    val email: String,
    val amount: Int
)

data class UpdateProfileRequest(
    val name: String? = null,
    val password: String? = null
)

object UserRole {
    const val ADMIN = "admin"
    const val GUEST = "guest"
}
