package com.example.thelastone.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 請求 (Request) 資料模型 ---

@Serializable
data class RecommendationForm(
    @SerialName("locations") val locations: List<String>,
    @SerialName("days") val days: Int,
    @SerialName("preferences") val preferences: List<String>,
    @SerialName("exclude") val exclude: List<String>,
    @SerialName("transportation") val transportation: String,
    @SerialName("notes") val notes: String? = null
)

@Serializable
data class RecommendRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("form") val form: RecommendationForm
)


// --- 回應 (Response) 資料模型 ---

@Serializable
data class Place(
    // 假設的欄位，請根據您後端 `used_places` 的實際結構調整
    @SerialName("name") val name: String? = "Unknown Place",
    @SerialName("place_id") val placeId: String? = null
)

@Serializable
data class RecommendationResponse(
    @SerialName("trip_name") val tripName: String,
    @SerialName("html") val itineraryHtml: String,
    @SerialName("markdown") val markdown: String,
    @SerialName("summary") val summary: String,
    @SerialName("days") val days: Int,
    @SerialName("used_places") val usedPlaces: List<Place>,
    @SerialName("locations_text") val locationsText: String,
    @SerialName("error") val error: Boolean,
    @SerialName("error_message") val errorMessage: String? = null
)