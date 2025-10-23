// 檔案路徑：vm/StartPreviewViewModel.kt
package com.example.thelastone.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.thelastone.data.mapper.toApiRequestForm // 👈 不再需要 ViewModel 直接用 Mapper
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
// import com.example.thelastone.data.remote.ApiService // 👈 ViewModel 不應該直接依賴 ApiService
// import com.example.thelastone.data.remote.RecommendationForm // 👈 ViewModel 不關心 API DTO
import com.example.thelastone.data.repo.TripRepository // 👈 ViewModel 應該依賴 Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- (GenerationState, NavigationEffect, StartPreviewUiState 保持不變) ---
sealed interface GenerationState {
    data object Idle : GenerationState
    data object Loading : GenerationState
    data class Success(val trip: Trip) : GenerationState // (應已是 Trip)
    data class Error(val message: String) : GenerationState
}
sealed interface NavigationEffect {
    data class NavigateToTripDetail(val tripId: String) : NavigationEffect
}
data class StartPreviewUiState(
    val form: TripForm? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val generationState: GenerationState = GenerationState.Idle
)
// --- (State/Effect/UiState 定義結束) ---

@HiltViewModel
class StartPreviewViewModel @Inject constructor(
    // 🔽🔽 1. 只注入 Repository 🔽🔽
    private val tripRepo: TripRepository
    // private val apiService: ApiService // 👈 移除 ApiService 注入
    // 🔼🔼
) : ViewModel() {

    private val _state = MutableStateFlow(StartPreviewUiState())
    val state = _state.asStateFlow()
    private val _navigationEffect = MutableSharedFlow<NavigationEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    init {
        loadForm()
    }

    private fun loadForm() {
        viewModelScope.launch {
            // 從 Repository 讀取由上一個畫面暫存的表單資料
            val form = tripRepo.getTripFormForPreview()
            _state.update {
                if (form != null) {
                    it.copy(form = form, loading = false)
                } else {
                    it.copy(error = "無法載入表單資料", loading = false)
                }
            }
        }
    }

    /**
     * 由 UI 呼叫，觸發 AI 行程生成
     * @param userId 目前登入的使用者 ID
     * @param excludeInput 使用者在預覽畫面輸入的「排除條件」字串 (這部分邏輯需要移到 Repository 或 UseCase)
     */
    fun generateItinerary(userId: String, excludeInput: String) {
        val currentForm = _state.value.form ?: return

        // ‼️ 理想情況下，"加入排除條件" 的邏輯應該在 Repository 或 UseCase 中完成，
        // ViewModel 只負責傳遞 UI 輸入。
        // 但我們先在這裡簡單處理，讓它能跑起來。
        // 我們假設 TripForm 有 extraNote 欄位可以放。
        val formWithExclusion = currentForm.copy(
            extraNote = (currentForm.extraNote ?: "") + "\nExclude: $excludeInput"
        )

        _state.update { it.copy(generationState = GenerationState.Loading) }

        viewModelScope.launch {
            try {
                // 2. ✅ 呼叫 tripRepo.createTrip，並傳入 userId
                val responseTrip = tripRepo.createTrip(formWithExclusion, userId)

                // 3. 成功
                _state.update {
                    it.copy(generationState = GenerationState.Success(responseTrip))
                }

            } catch (e: Exception) {
                // 4. 失敗
                e.printStackTrace()
                _state.update {
                    it.copy(generationState = GenerationState.Error(e.message ?: "未知的網路錯誤"))
                }
            }
        }
    }

    /**
     * 由 UI 上的「儲存並完成」按鈕呼叫
     * (這個函式現在邏輯正確)
     */
    fun onConfirmAndSave() {
        viewModelScope.launch {
            val currentState = _state.value.generationState
            if (currentState is GenerationState.Success) {
                val tripId = currentState.trip.id
                if (tripId.isNotBlank()) {
                    _navigationEffect.emit(NavigationEffect.NavigateToTripDetail(tripId))
                }
            }
        }
    }
}