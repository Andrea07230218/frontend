package com.example.thelastone.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastone.data.model.PlaceLite
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.repo.SpotRepository
import com.example.thelastone.data.repo.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
// 🔽🔽 1. 移除 'Flow' 相關的 import (如果不再需要) 🔽🔽
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SpotsSource { TAIWAN, AROUND_ME }

data class ExploreUiState(
    val isLoading: Boolean = true, // 👈 這個現在會用來顯示 Trips 的載入
    val error: String? = null,

    // Trips
    val popularTrips: List<Trip> = emptyList(),
    // 🔽🔽 2. 移除了 isRefreshing 🔽🔽
    // val isRefreshing: Boolean = false,

    // Spots (保持不變)
    val spots: List<PlaceLite> = emptyList(),
    val spotsLoading: Boolean = false,
    val spotsError: String? = null,
    val spotsInitialized: Boolean = false,
    val spotsSource: SpotsSource = SpotsSource.TAIWAN // 👈 保持
)



// ExploreViewModel.kt
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tripRepo: TripRepository,
    private val spotRepo: SpotRepository
) : ViewModel() {

    // 🔽🔽 3. 移除了 refresh, popularTripsFlow, popularResource 🔽🔽
    // private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    // private fun popularTripsFlow(): Flow<List<Trip>> = ...
    // private val popularResource: Flow<Result<List<Trip>>> = ...

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        // 🔽🔽 4. 修改 init 區塊 🔽🔽
        // 移除原本的 Trips 區塊 (popularResource.scan...)

        // 直接呼叫 API 載入「通用推薦」行程
        loadGeneralTrips()

        // Spots 由畫面決定 (邏輯不變)
    }

    /**
     * 🔽🔽 5. 新增這個函式，用來載入「通用行程」 🔽🔽
     */
    private fun loadGeneralTrips() {
        viewModelScope.launch {
            // 開始載入，顯示 Loading
            _state.update { it.copy(isLoading = true, error = null) }

            runCatching {
                // 呼叫我們在 Repository 新增的函式
                tripRepo.fetchGeneralRecommendations()
            }.onSuccess { trips ->
                // 成功，更新 popularTrips 列表
                _state.update {
                    it.copy(
                        isLoading = false,
                        popularTrips = trips
                    )
                }
            }.onFailure { e ->
                // 失敗，顯示錯誤訊息
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "熱門行程載入失敗"
                    )
                }
            }
        }
    }

    // 🔽🔽 6. 修改 refresh 和 retry 🔽🔽
    fun refresh() {
        // 刷新時，重新載入通用行程
        loadGeneralTrips()
        // Spots 的刷新交給畫面 (邏輯不變)
    }
    fun retry() = loadGeneralTrips() // 重試時，也重新載入通用行程

    // ====== Spots 相關的函式 (保持不變) ======

    /** 使用者同意定位後：載入使用者附近 */
    fun loadSpotsAroundMe(
        userId: String? = null,
        limit: Int = 30,
        lat: Double,
        lng: Double,
        radiusMeters: Int = 5000,
        openNow: Boolean? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(spotsLoading = true, spotsError = null) }
            runCatching {
                spotRepo.getRecommendedSpots(userId, limit, lat, lng, radiusMeters, openNow)
            }.onSuccess { list ->
                _state.update { it.copy(spots = list, spotsLoading = false, spotsInitialized = true, spotsSource = SpotsSource.AROUND_ME) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        spotsError = e.message ?: "熱門景點載入失敗",
                        spotsLoading = false,
                        spotsInitialized = true // ✅ 失敗也算已初始化，避免顯示「重試」閃爍
                    )
                }
            }
        }
    }

    fun loadSpotsTaiwan(userId: String? = null, limit: Int = 30) {
        viewModelScope.launch {
            _state.update { it.copy(spotsLoading = true, spotsError = null) }
            runCatching { spotRepo.getTaiwanPopularSpots(userId, limit) }
                .onSuccess { list ->
                    _state.update { it.copy(spots = list, spotsLoading = false, spotsInitialized = true, spotsSource = SpotsSource.TAIWAN) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            spotsError = e.message ?: "熱門景點載入失敗",
                            spotsLoading = false,
                            spotsInitialized = true
                        )
                    }
                }
        }
    }
}

