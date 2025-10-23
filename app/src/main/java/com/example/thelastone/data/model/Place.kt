// 檔案路徑：data/model/Place.kt
package com.example.thelastone.data.model

import kotlinx.serialization.Serializable

@Serializable // 👈 1. 加上這個標註
data class Place(
    val placeId: String,
    val name: String,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val address: String?,
    val openingHours: List<String> = emptyList(),
    val openNow: Boolean? = null,
    val openStatusText: String? = null,
    val lat: Double,
    val lng: Double,
    val photoUrl: String? = null,
    val miniMapUrl: String? = null
)

@Serializable // 👈 2. 最好也幫這個加上
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String?,
    val lat: Double,
    val lng: Double,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val photoUrl: String?,
    val types: List<String> = emptyList(),
    val websiteUri: String? = null,
    val nationalPhoneNumber: String? = null,
    val priceLevel: Int? = null,
    val openingHours: List<String> = emptyList(),
    val openNow: Boolean? = null,
    val openStatusText: String? = null
)

@Serializable
data class PlaceLite(
    val placeId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val photoUrl: String? = null,
    val openingHours: List<String> = emptyList(),
    val openNow: Boolean? = null,
    val openStatusText: String? = null
)

// (Functions remain the same)
fun Place.toLite() = PlaceLite(
    placeId = placeId,
    name = name,
    lat = lat,
    lng = lng,
    address = address,
    rating = rating,
    userRatingsTotal = userRatingsTotal,
    photoUrl = photoUrl,
    openingHours = openingHours,
    openNow = null,
    openStatusText = null
)

fun PlaceLite.toFull(): Place = Place(
    placeId = placeId,
    name = name,
    rating = rating,
    userRatingsTotal = userRatingsTotal,
    address = address,
    lat = lat,
    lng = lng,
    photoUrl = photoUrl,
    openingHours = openingHours,
    miniMapUrl = null
)