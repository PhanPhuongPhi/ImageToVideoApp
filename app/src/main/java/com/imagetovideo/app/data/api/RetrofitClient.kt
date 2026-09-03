package com.imagetovideo.app.data.api

import android.content.Context
import com.imagetovideo.app.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // URL từ Ngrok Kaggle
    // private const val BASE_URL = "https://defection-rimless-bobble.ngrok-free.dev/"

    private const val BASE_URL = "http://10.0.2.2:8000/"
    
    fun getBaseUrl(): String = BASE_URL

    fun getApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()
            val builder = originalRequest.newBuilder()

            if (!token.isNullOrEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }

            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun resolveMediaUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        val cleanBaseUrl = BASE_URL.removeSuffix("/")
        val cleanUrl = if (url.startsWith("/")) url else "/$url"
        return cleanBaseUrl + cleanUrl
    }
}
