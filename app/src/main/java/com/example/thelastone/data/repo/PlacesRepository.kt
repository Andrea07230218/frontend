package com.example.thelastone.data.repo

import com.example.thelastone.data.model.PlaceDetails
import com.example.thelastone.data.model.PlaceLite
import com.example.thelastone.data.remote.LatLng

// RankPreference enum 應該也在這個檔案或已被 import
enum class RankPreference { DISTANCE, POPULARITY }

interface PlacesRepository {

    // 🔽 [ [ [ 請在這裡加上新的函式宣告 ] ] ] 🔽
    suspend fun getDeviceLocation(): LatLng?

    fun buildPhotoUrl(photoName: String, maxWidth: Int = 400): String

    suspend fun searchText(
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        radiusMeters: Int? = null,
        openNow: Boolean? = null
    ): List<PlaceLite>

    suspend fun searchNearby(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        includedTypes: List<String>,
        rankPreference: RankPreference = RankPreference.DISTANCE,
        openNow: Boolean? = null,
        maxResultCount: Int = 10
    ): List<PlaceLite>

    suspend fun fetchDetails(placeId: String): PlaceDetails
}
