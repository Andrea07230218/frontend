package com.example.thelastone.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 請求 (Request) 資料模型 ---

/**
 * 代表發送給 /recommend API 的請求 Body 結構
 * (這個 Wrapper class 保持不變)
 */
@Serializable
data class ApiRecommendRequest(
    @SerialName("user_id") // 確保 JSON key 和 Python 一致
    val userId: String,

    val form: RecommendationForm // 把 API 專用模型包在這裡
)

/**
 * 這是 API (FastAPI) 實際接收的表單內容。
 * ✅ 已加入所有遺失的欄位
 */
@Serializable
data class RecommendationForm(
    // --- 你原本就有的欄位 ---
    @SerialName("locations") val locations: List<String>, // (例如 ["台南"])
    @SerialName("days") val days: Int,
    @SerialName("preferences") val preferences: List<String>, // (來自 TripForm.styles)
    @SerialName("exclude") val exclude: List<String>,
    @SerialName("transportation") val transportation: String, // (來自 TripForm.transportPreferences)
    @SerialName("notes") val notes: String? = null, // (來自 TripForm.extraNote)

    // --- 🔽🔽 ‼️ 加入這些遺失的欄位 ‼️ 🔽🔽 ---

    @SerialName("trip_name")
    val tripName: String, // 👈 對應問題 1 (來自 TripForm.name)

    @SerialName("start_date")
    val startDate: String, // 👈 對應問題 2 & 3 (來自 TripForm.startDate)

    @SerialName("end_date")
    val endDate: String,   // 👈 對應問題 2 & 3 (來自 TripForm.endDate)

    @SerialName("activity_start")
    val activityStart: String?,

    @SerialName("activity_end")
    val activityEnd: String?,

    @SerialName("total_budget")
    val totalBudget: Int?, // 👈 對應問題 4 (來自 TripForm.totalBudget)

    @SerialName("avg_age")
    val avgAge: String,    // 👈 對應問題 4 (來自 TripForm.avgAge.name)

    @SerialName("use_gmaps_rating")
    val useGmapsRating: Boolean, // 👈 對應問題 4 (來自 TripForm.useGmapsRating)

    @SerialName("visibility")
    val visibility: String     // 👈 對應問題 4 (來自 TripForm.visibility.name)
)

/**
 * 這是你原檔案中的舊模型，先保留不動，
 * 雖然 ApiRecommendRequest 已經取代了它的功能。
 */
@Serializable
data class RecommendRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("form") val form: RecommendationForm
)


// --- 回應 (Response) 資料模型 (保持不變) ---

@Serializable
data class Place(
    @SerialName("name") val name: String? = "Unknown Place",
    @SerialName("place_id") val placeId: String? = null
)

@Serializable
data class RecommendationResponse(
    @SerialName("trip_id") val tripId: String,
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