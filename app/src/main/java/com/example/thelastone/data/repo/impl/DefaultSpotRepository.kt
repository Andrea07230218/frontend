package com.example.thelastone.data.repo.impl // 確保 package 在 impl 子目錄下

import com.example.thelastone.data.model.PlaceLite
import com.example.thelastone.data.repo.PlacesRepository // 確保 import 正確
import com.example.thelastone.data.repo.SpotRepository   // 確保 import 正確
import javax.inject.Inject

// data/repo/impl/DefaultSpotRepository.kt
class DefaultSpotRepository @Inject constructor(
    private val placesRepo: PlacesRepository
) : SpotRepository { // 確保有 ": SpotRepository"

    override suspend fun getRecommendedSpots(
        userId: String?,
        limit: Int,
        lat: Double?, // 雖然接收了 lat
        lng: Double?, // 雖然接收了 lng
        radiusMeters: Int?,
        openNow: Boolean?
    ): List<PlaceLite> {
        println("DefaultSpotRepository: getRecommendedSpots called BUT FORCING TAIPEI COORDS") // 修改 Log

        // 🔽🔽 【強制】無論傳入什麼，都使用台北座標 🔽🔽
        val forceLat = 25.0330
        val forceLng = 121.5654
        val forceRadius = 20000 // 半徑 20 公里
        // 🔼🔼

        return placesRepo.searchText(
            query = "top tourist attractions Taipei", // Query 也改成台北
            lat = forceLat, // 使用強制座標
            lng = forceLng, // 使用強制座標
            radiusMeters = forceRadius, // 使用強制半徑
            openNow = openNow
        ).take(limit)
    }

    override suspend fun getTaiwanPopularSpots(userId: String?, limit: Int): List<PlaceLite> {
        println("DefaultSpotRepository: getTaiwanPopularSpots called")

        val taipeiLat = 25.0330
        val taipeiLng = 121.5654

        return placesRepo.searchText(
            query = "top tourist attractions Taipei",
            lat = taipeiLat,
            lng = taipeiLng,
            radiusMeters = 20000,
            openNow = null
        ).take(limit)
    }
}

