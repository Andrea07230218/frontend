// 檔案路徑：ui/screens/recommend/RecommendationViewModel.kt
package com.example.thelastone.ui.screens.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 從 RecommendationState.kt 導入 Form 和 State
import com.example.thelastone.ui.screens.recommend.RecommendationForm
import com.example.thelastone.ui.screens.recommend.RecommendationUiState
// 你的資料層模型
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.repo.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val tripRepository: TripRepository // 注入 Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    /**
     * 從 UI 呼叫此函式以獲取 AI 推薦
     * ✅ 已加回 userId 參數
     *
     * @param userId 目前登入的使用者 ID
     * @param form 從 Compose UI 收集到的表單資料 (RecommendationForm)
     */
    // 🔽🔽 1. 加回 userId 參數 🔽🔽
    fun fetchRecommendations(userId: String, form: RecommendationForm) {
        // 🔼🔼
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            try {
                // 2. 將 UI 表單 (RecommendationForm) 轉換為資料層表單 (TripForm)
                val tripForm: TripForm = form.toTripForm()

                // 3. ✅ 呼叫 Repository 時傳入 userId
                val resultTrip = tripRepository.createTrip(tripForm, userId)

                _uiState.value = RecommendationUiState.Success(resultTrip)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = RecommendationUiState.Error(e.message ?: "無法生成行程")
            }
        }
    }
}

/**
 * 「翻譯函式」：將 UI 層的 `RecommendationForm` 轉換為
 * 資料層/API 期望的 `TripForm`。
 * (這個函式不需要 userId，保持不變)
 */
private fun RecommendationForm.toTripForm(): TripForm {
    return TripForm(
        locations = this.locations,
        name = this.tripName,
        totalBudget = this.totalBudget,
        startDate = this.startDate,
        endDate = this.endDate,
        activityStart = this.activityStart,
        activityEnd = this.activityEnd,
        transportPreferences = this.transportPreferences,
        useGmapsRating = this.useGmapsRating,
        styles = this.styles,
        avgAge = this.avgAge,
        visibility = this.visibility,
        extraNote = this.extraNote,
        aiDisclaimerChecked = this.aiDisclaimerChecked
    )
}