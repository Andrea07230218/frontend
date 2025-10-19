package com.example.thelastone.di

import com.example.thelastone.BuildConfig
import com.example.thelastone.data.remote.PlacesApi
import com.example.thelastone.data.repo.PlacesRepository
import com.example.thelastone.data.repo.impl.PlacesRepositoryImpl
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory // ⚠️ 您的 import 可能是 com.jakewharton.retrofit.retrofit2-kotlinx-serialization-converter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

// PlacesModule.kt (已修正參數)
@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Provides @Singleton @GoogleApi
    fun provideOkHttp(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(java.time.Duration.ofSeconds(30))
            .connectTimeout(java.time.Duration.ofSeconds(15))
            .readTimeout(java.time.Duration.ofSeconds(30))
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    // ❗ 您的 Google API Key 是用 Header 傳送，這很棒！
                    .addHeader("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
                        level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(logging)
                }
            }
            .build()

    @Provides @Singleton @GoogleApi
    fun provideRetrofit(
        @GoogleApi okHttp: OkHttpClient, // 👈 加上 @GoogleApi 標籤
        json: Json // 假設這來自您的 SerializationModule
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://places.googleapis.com/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton @GoogleApi
    fun providePlacesApi(
        @GoogleApi retrofit: Retrofit // 👈 加上 @GoogleApi 標籤
    ): PlacesApi =
        retrofit.create(PlacesApi::class.java)

    @Provides @Singleton
    fun providePlacesRepository(
        @GoogleApi api: PlacesApi // 👈 加上 @GoogleApi 標籤
    ): PlacesRepository =
        PlacesRepositoryImpl(api)
}