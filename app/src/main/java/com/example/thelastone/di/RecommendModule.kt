package com.example.thelastone.di

import com.example.thelastone.data.remote.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit // 👈 1. 加入 Time Unit 的 import
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecommendModule {

    /**
     * 您的後端 API 的 Base URL。
     * 對於 Android 模擬器，必須使用 10.0.2.2 來指向您電腦的 localhost。
     * ‼️ 已移除 URL 前後的空格 ‼️
     */
    // 🔽🔽 2. 修正：移除 URL 前後的空格 🔽🔽
    private const val RECOMMEND_BASE_URL = "http://10.0.2.2:8000/"
    // 🔼🔼

    @Provides
    @Singleton
    @RecommendApi // 標記為 "推薦 API" 專用
    fun provideRecommendOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            // 🔽🔽 3. 加入這三行，把 Timeout 延長到 60 秒 🔽🔽
            .connectTimeout(600, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .writeTimeout(600, TimeUnit.SECONDS)
            // 🔼🔼
            .build()
    }

    @Provides
    @Singleton
    @RecommendApi // 標記
    fun provideRecommendRetrofit(
        @RecommendApi okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(RECOMMEND_BASE_URL) // 👈 會使用修正後的 URL
            .client(okHttpClient)        // 👈 會使用修正後的 OkHttpClient
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        @RecommendApi retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}