package com.example.thelastone.di

import com.example.thelastone.data.repo.PlacesRepository
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.data.repo.impl.PlacesRepositoryImpl
import com.example.thelastone.data.repo.impl.TripRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 這個 Hilt 模組負責提供資料儲存庫 (Repository) 的依賴。
 * @Module 標註表示這是一個 Hilt 模組。
 * @InstallIn(SingletonComponent::class) 表示這個模組中的綁定在整個 App 的生命週期內有效。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * 這個函式使用 @Binds 標註，告訴 Hilt 如何綁定 TripRepository 介面
     * 和它的具體實作 (TripRepositoryImpl)。
     * 這是 Hilt 中推薦的、效能最好的綁定方式。
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository

    /**
     * 這個函式同樣使用 @Binds 標註，告訴 Hilt 如何綁定 PlacesRepository 介面
     * 和它的具體實作 (PlacesRepositoryImpl)。
     */
    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        impl: PlacesRepositoryImpl
    ): PlacesRepository

}

