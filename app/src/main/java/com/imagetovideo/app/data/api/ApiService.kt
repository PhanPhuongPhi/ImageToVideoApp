package com.imagetovideo.app.data.api

import com.imagetovideo.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, String>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserProfile>

    @PATCH("auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>

    @GET("users/credits")
    suspend fun getCredits(): Response<Map<String, Any>>

    @Multipart
    @POST("generate-video")
    suspend fun generateVideo(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody,
        @Part("ratio") ratio: RequestBody
    ): Response<VideoJob>

    @GET("status/{job_id}")
    suspend fun getVideoStatus(@Path("job_id") jobId: String): Response<VideoJob>

    @GET("videos/history")
    suspend fun getVideoHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<VideoHistory>

    @DELETE("videos/{video_id}")
    suspend fun deleteVideo(@Path("video_id") videoId: String): Response<Map<String, String>>

    @POST("admin/grant-credits")
    suspend fun grantCredits(@Body request: GrantCreditsRequest): Response<Map<String, Any>>

    // --- Credit & Payments ---
    @GET("credits/history")
    suspend fun getCreditHistory(): Response<TransactionHistoryResponse>

    @GET("credits/packages")
    suspend fun getCreditPackages(): Response<CreditPackagesResponse>

    @POST("credits/purchase/{package_id}")
    suspend fun purchaseCredit(@Path("package_id") packageId: String): Response<Map<String, Any>>

    // --- Promotions ---
    @GET("promotions/active")
    suspend fun getActivePromotions(): Response<List<Promotion>>

    // --- Admin Dashboard & Management ---
    @GET("admin/dashboard/stats")
    suspend fun getAdminStats(): Response<AdminDashboardStats>

    @POST("admin/promotions")
    suspend fun createPromotion(@Body promotion: Promotion): Response<Promotion>

    @GET("admin/users")
    suspend fun getAllUsers(): Response<List<UserProfile>>

    @POST("admin/users/{user_id}/status")
    suspend fun updateUserStatus(
        @Path("user_id") userId: String,
        @Body status: Map<String, Boolean>
    ): Response<Map<String, String>>

    @GET("admin/settings")
    suspend fun getSettings(): Response<List<SystemSetting>>

    @POST("admin/settings")
    suspend fun updateSetting(@Body setting: SystemSetting): Response<SystemSetting>

    @GET("admin/videos")
    suspend fun getAllAdminVideos(): Response<List<AdminVideoItem>>

    @DELETE("admin/videos/{video_id}")
    suspend fun adminDeleteVideo(@Path("video_id") videoId: String): Response<Map<String, String>>
}
