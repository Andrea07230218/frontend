package com.example.thelastone.di

import com.example.thelastone.data.repo.PlacesRepository
import com.example.thelastone.data.repo.StartRepository // 👈 1. 確保 Import StartRepository
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.data.repo.impl.PlacesRepositoryImpl
import com.example.thelastone.data.repo.impl.StartRepositoryImpl // 👈 2. 確保 Import StartRepositoryImpl
import com.example.thelastone.data.repo.impl.TripRepositoryImpl
// import com.example.thelastone.data.repo.impl.fake.StartRepositoryFake // 不需要 Fake 了

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 這個 Hilt 模組負責提供資料儲存庫 (Repository) 的依賴。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * 綁定 TripRepository 介面到 TripRepositoryImpl 實作。
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository

    /**
     * 綁定 PlacesRepository 介面到 PlacesRepositoryImpl 實作。
     */
    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        impl: PlacesRepositoryImpl
    ): PlacesRepository

    // --- 🔽🔽 3. 【新增這個綁定】 🔽🔽 ---
    /**
     * 綁定 StartRepository 介面到 StartRepositoryImpl 實作。
     * 這會告訴 Hilt 使用真正的 API 呼叫，而不是 Fake 資料。
     */
    @Binds
    @Singleton
    abstract fun bindStartRepository(
        impl: StartRepositoryImpl // 👈 指向真正的 Impl
    ): StartRepository
    // --- 🔼🔼 ---

}