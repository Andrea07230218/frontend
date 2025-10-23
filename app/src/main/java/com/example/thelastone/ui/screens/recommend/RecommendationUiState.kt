// 檔案路徑：ui/screens/recommend/RecommendationState.kt
package com.example.thelastone.ui.screens.recommend

import com.example.thelastone.data.model.AgeBand
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripVisibility

/**
 * 唯一的 UI 狀態定義
 * sealed interface 來表示畫面的不同狀態
 */
sealed interface RecommendationUiState {
    object Idle : RecommendationUiState
    object Loading : RecommendationUiState
    data class Success(val trip: Trip) : RecommendationUiState // ✅ 包含 .trip
    data class Error(val errorMessage: String) : RecommendationUiState // ✅ 包含 .errorMessage
}

/**
 * 唯一的 UI 表單模型 (Form)
 * 這份 data class 必須与你的 UI 畫面收集的欄位保持一致
 */
data class RecommendationForm(
    val tripName: String,
    val locations: String,
    val totalBudget: Int?,
    val startDate: String,
    val endDate: String,
    val activityStart: String?,
    val activityEnd: String?,
    val transportPreferences: List<String>,
    val useGmapsRating: Boolean,
    val styles: List<String>,
    val avgAge: AgeBand,
    val visibility: TripVisibility = TripVisibility.PRIVATE,
    val extraNote: String? = null,
    val aiDisclaimerChecked: Boolean = false
)