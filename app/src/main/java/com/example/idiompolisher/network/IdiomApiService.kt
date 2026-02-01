package com.example.idiompolisher.network

import com.example.idiompolisher.data.IdiomResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Request body model
data class PolishingRequest(val text: String)

interface IdiomApiService {
    @POST("/polish") // Endpoint path to be confirmed with backend
    suspend fun polishText(@Body request: PolishingRequest): IdiomResponse
}

import com.example.idiompolisher.BuildConfig

object RetrofitClient {
    // Backend URL from local.properties (via BuildConfig)
    private const val BASE_URL = BuildConfig.BACKEND_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 连接超时 60s
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时 60s
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时 60s
        .build()

    val apiService: IdiomApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IdiomApiService::class.java)
    }
}