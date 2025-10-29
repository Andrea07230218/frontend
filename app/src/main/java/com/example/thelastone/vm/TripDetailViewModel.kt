// 檔案路徑：vm/TripDetailViewModel.kt
package com.example.thelastone.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.Alternative // 👈 1. 加入 Import
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.di.SessionManager
import com.example.thelastone.utils.TripPerms
import com.example.thelastone.utils.computePerms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// (LocalTime 和 Formatter 已經不需要了，但保留也無妨)
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// (這兩個 helper 函式已不再需要)
// private val FMT_HH_MM = DateTimeFormatter.ofPattern("H:mm")
// private fun parseLocalTimeOrNull(t: String?): LocalTime? = ...

sealed interface TripDetailUiState {
    data object Loading : TripDetailUiState
    data class Data(val trip: Trip) : TripDetailUiState
    data class Error(val message: String) : TripDetailUiState
}

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repo: TripRepository,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])
    private val retry = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _perms = MutableStateFlow<TripPerms?>(null)
    val perms: StateFlow<TripPerms?> = _perms


    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TripDetailUiState> =
        retry.onStart { emit(Unit) }
            .flatMapLatest {
                repo.observeTripDetail(tripId)
                    .onEach { trip ->
                        // 每次資料更新時重算權限
                        val uid = session.currentUserId
                        _perms.value = trip.computePerms(uid)
                    }
                    // 🔽🔽 ‼️ 修正：移除 .sortedByStartTime() ‼️ 🔽🔽
                    .map<Trip, TripDetailUiState> { TripDetailUiState.Data(it) }
                    // 🔼🔼
                    .catch { emit(TripDetailUiState.Error(it.message ?: "Load failed")) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TripDetailUiState.Loading
            )

    // 🔽🔽 2. 在這裡貼上新函式 🔽🔽
    /**
     * 執行更換行程 API
     * @param oldActivity 要被換掉的舊活動
     * @param replacement 用來替換的新活動 (從 Alternative Dialog 傳來)
     */
    fun replaceActivity(oldActivity: Activity, replacement: Alternative) {
        viewModelScope.launch {
            // 這裡我們假設你的 Repository 有一個 "replaceActivityInTrip" 函式
            // 你的 Repository 函式名稱可能不同
            runCatching {
                repo.replaceActivityInTrip(
                    tripId = tripId,
                    oldActivityId = oldActivity.id,
                    newActivityData = replacement
                )
            }.onSuccess {
                // 成功後，我們呼叫 reload() 來重新整理畫面
                reload()
            }.onFailure {
                // TODO: 這裡應該要顯示錯誤訊息給使用者 (例如用 SnackBar)
                it.printStackTrace() // 暫時先印出錯誤
            }
        }
    }
    // 🔼🔼

    /**
     * ‼️ 警告：這個函式 (removeActivity) 也與新的 Slot 結構不相容
     * 暫時拋出錯誤
     */
    fun removeActivity(dayIndex: Int, activityIndex: Int) {
        viewModelScope.launch {
            // runCatching { repo.removeActivity(tripId, dayIndex, activityIndex) } // 👈 舊邏輯
            //    .onFailure { /* TODO: snackbar */ }

            throw NotImplementedError("removeActivity logic needs refactoring for Slot-based model")
        }
    }

    fun reload() { retry.tryEmit(Unit) }
}

// 🔽🔽 ‼️ 刪除 (或註解掉) 整個 sortedByStartTime 函式 ‼️ 🔽🔽
/*
private fun Trip.sortedByStartTime(): Trip = copy(
    days = days.map { day ->
        day.copy(
            activities = day.activities.sortedWith( // 👈 錯誤點
                compareBy<Activity> { parseLocalTimeOrNull(it.startTime) ?: LocalTime.MAX }
            )
        )
    }
)
*/
// 🔼🔼