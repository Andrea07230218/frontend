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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecommendModule {

    // 您的後端 API 的 Base URL
    private const val RECOMMEND_BASE_URL = "http://10.0.2.2:8000/"

    @Provides
    @Singleton
    @RecommendApi // 👈 標記為 "推薦 API" 專用
    fun provideRecommendOkHttpClient(): OkHttpClient {
        // 這是給 "推薦 API" 用的，只需要 Log
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    @RecommendApi // 👈 標記
    fun provideRecommendRetrofit(
        @RecommendApi okHttpClient: OkHttpClient, // 👈 指定使用 @RecommendApi 的 client
        json: Json // 👈 來自您的 SerializationModule
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(RECOMMEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType)) // 👈 'asConverterFactory' 錯誤會消失
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        @RecommendApi retrofit: Retrofit // 👈 告訴 Hilt 要用 @RecommendApi 的 Retrofit
    ): ApiService {
        // ✅ 正確：使用 ::class.java，並且後面沒有括號
        return retrofit.create(ApiService::class.java)
    }
}