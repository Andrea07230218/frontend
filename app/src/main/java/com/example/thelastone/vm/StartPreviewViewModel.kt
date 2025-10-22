package com.example.thelastone.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastone.data.mapper.toApiRequestForm
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.remote.ApiService
import com.example.thelastone.data.remote.RecommendRequest
import com.example.thelastone.data.remote.RecommendationResponse
import com.example.thelastone.data.repo.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI 行程生成過程的 UI 狀態
 */
sealed interface GenerationState {
    data object Idle : GenerationState // 閒置狀態，尚未開始生成
    data object Loading : GenerationState // 正在呼叫 API，生成中
    data class Success(val response: RecommendationResponse) : GenerationState // 生成成功
    data class Error(val message: String) : GenerationState // 生成失敗
}

/**
 * 一次性的導航指令 (Side-Effect)
 */
sealed interface NavigationEffect {
    data class NavigateToTripDetail(val tripId: String) : NavigationEffect
}

/**
 * 預覽畫面的整體 UI 狀態
 */
data class StartPreviewUiState(
    val form: TripForm? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val generationState: GenerationState = GenerationState.Idle // 追蹤 API 呼叫狀態
)

@HiltViewModel
class StartPreviewViewModel @Inject constructor(
    private val tripRepo: TripRepository,
    private val apiService: ApiService // 注入我們自己的 API Service
) : ViewModel() {

    private val _state = MutableStateFlow(StartPreviewUiState())
    val state = _state.asStateFlow()

    // 🔽 [新增] 使用 SharedFlow 來發送「導航」等一次性指令 🔽
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
     * @param excludeInput 使用者在預覽畫面輸入的「排除條件」字串
     */
    fun generateItinerary(userId: String, excludeInput: String) {
        val currentForm = _state.value.form ?: return

        // 1. 將使用者輸入的排除條件字串轉為列表
        val excludeTerms = excludeInput.split(Regex("[,、，\\s]+"))
            .filter { it.isNotBlank() }

        // 2. 更新 UI 狀態為「生成中」
        _state.update { it.copy(generationState = GenerationState.Loading) }

        viewModelScope.launch {
            try {
                // 3. 使用 Mapper 將 UI 表單轉換為 API 請求格式
                val apiRequestForm = currentForm.toApiRequestForm(excludeTerms)
                val request = RecommendRequest(userId = userId, form = apiRequestForm)

                // 4. 呼叫 API
                val response = apiService.getRecommendations(request)

                // 5. 根據 API 回應更新 UI 狀態
                if (response.error) {
                    _state.update {
                        it.copy(generationState = GenerationState.Error(response.errorMessage ?: "API 回報錯誤"))
                    }
                } else {
                    _state.update {
                        it.copy(generationState = GenerationState.Success(response))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(generationState = GenerationState.Error(e.message ?: "未知的網路錯誤"))
                }
            }
        }
    }

    /**
     * 由 UI 上的「儲存並完成」按鈕呼叫
     */
    fun onConfirmAndSave() {
        viewModelScope.launch {
            // 從目前的成功狀態中取得 tripId
            val currentState = _state.value.generationState
            if (currentState is GenerationState.Success) {
                val tripId = currentState.response.tripId
                if (tripId.isNotBlank()) {
                    // 發送「導航」指令給 UI
                    _navigationEffect.emit(NavigationEffect.NavigateToTripDetail(tripId))
                }
            }
        }
    }
}

