// 檔案路徑：vm/AddActivityViewModel.kt
package com.example.thelastone.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.DaySchedule // 👈 確保 import
import com.example.thelastone.data.model.PlaceLite
import com.example.thelastone.data.model.Slot       // 👈 確保 import
import com.example.thelastone.data.model.Trip
// 🔽🔽 ‼️ 修正：我們需要新的 toFullActivity 和 toPlaceLite 函式 ‼️ 🔽🔽
// import com.example.thelastone.data.model.toFull // 👈 舊的 toFull 已失效
// import com.example.thelastone.data.model.toLite // 👈 舊的 toLite 已失效
// 🔼🔼
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.utils.decodePlaceArg
import com.example.thelastone.utils.findDayIndexByDate
import com.example.thelastone.utils.millisToDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow // 👈 確保 import
import kotlinx.coroutines.flow.firstOrNull // 👈 確保 import
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

// ui/add/AddActivityUiState.kt (保持不變)
data class AddActivityUiState(
    val phase: Phase = Phase.Loading,
    val trip: Trip? = null,
    val place: PlaceLite? = null,
    val selectedDateMillis: Long? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val note: String? = null,
    val submitting: Boolean = false,
) {
    sealed interface Phase {
        data object Loading : Phase
        data class Error(val message: String) : Phase
        data object Ready : Phase
    }
}

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    private val repo: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])
    private val placeLiteFromArg: PlaceLite? =
        savedStateHandle.get<String>("placeJson")?.let { decodePlaceArg(it) }

    sealed class Mode {
        data object Add : Mode()
        data class Edit(val activityId: String) : Mode()
    }
    private val mode: Mode = run {
        val aid = savedStateHandle.get<String>("activityId")
        if (!aid.isNullOrBlank()) Mode.Edit(aid) else Mode.Add
    }
    val editing: Boolean get() = mode is Mode.Edit

    private val _state = MutableStateFlow(AddActivityUiState(place = placeLiteFromArg))
    // ✅ 保持你原有的 .asStateFlow()
    val state: StateFlow<AddActivityUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    // ✅ 保持你原有的 .asSharedFlow()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    sealed interface Effect { data class NavigateToDetail(val tripId: String) : Effect }

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(phase = AddActivityUiState.Phase.Loading) }
        runCatching {
            // ✅ 修正：repo.getTripDetail 尚未實作 (在 Impl 中是 TODO)
            // 我們改用 observeTripDetail 並只取第一筆資料
            repo.observeTripDetail(tripId).firstOrNull()
                ?: throw IllegalStateException("Trip $tripId not found")
        }
            .onSuccess { t: Trip -> // 👈 t 是 Trip 物件
                when (val m = mode) {
                    Mode.Add -> {
                        // 處理 startDate 可能為 null 的情況
                        val defaultDate = t.startDate ?: LocalDate.now().toString()
                        val defaultMillis = LocalDate.parse(defaultDate, DATE_FMT)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        _state.update {
                            it.copy(
                                phase = AddActivityUiState.Phase.Ready,
                                trip = t,
                                selectedDateMillis = it.selectedDateMillis ?: defaultMillis
                            )
                        }
                    }
                    is Mode.Edit -> {
                        // 🔽🔽 ‼️ 修正：使用新的 findActivityLocation 函式 ‼️ 🔽🔽
                        val location = findActivityLocation(t, m.activityId)
                            ?: return@onSuccess _state.update {
                                it.copy(phase = AddActivityUiState.Phase.Error("找不到要編輯的活動"))
                            }

                        // 從 location 解構出需要的物件
                        val (dayIdx, slotIdx, actIdx, act, slot, day) = location

                        val millis = LocalDate.parse(day.date, DATE_FMT) // 假設 day.date 格式正確
                            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        _state.update {
                            it.copy(
                                phase = AddActivityUiState.Phase.Ready,
                                trip = t,
                                selectedDateMillis = millis,
                                place = act.toPlaceLite(), // 👈 ✅ 修正：使用 'act.toPlaceLite()'
                                startTime = slot.window.firstOrNull(), // 👈 ✅ 修正：從 slot 讀取
                                endTime   = slot.window.lastOrNull(),   // 👈 ✅ 修正：從 slot 讀取
                                note      = act.note // 👈 ✅ 修正：讀取 'act.note' (來自 helper)
                            )
                        }
                        // 🔼🔼
                    }
                }
            }
            .onFailure { e ->
                e.printStackTrace() // 印出錯誤
                _state.update { it.copy(phase = AddActivityUiState.Phase.Error(e.message ?: "載入失敗")) }
            }
    }

    // (loadForEdit 和 initForCreate 保持不變)
    suspend fun loadForEdit(tripId: String, activityId: String) { reload() }
    fun initForCreate(tripId: String, placeJson: String) {}
    fun fail(message: String) { _state.update { it.copy(phase = AddActivityUiState.Phase.Error(message)) } }

    // (update* 函式保持不變)
    fun updateDate(millis: Long?)   { _state.update { it.copy(selectedDateMillis = millis) } }
    fun updateStartTime(v: String?) { _state.update { it.copy(startTime = v?.ifBlank { null }) } }
    fun updateEndTime(v: String?)   { _state.update { it.copy(endTime = v?.ifBlank { null }) } }
    fun updateNote(v: String?)      { _state.update { it.copy(note = v?.ifBlank { null }) } }

    /**
     * ‼️ 警告：此函式基於舊的資料模型 (List<Activity>)，
     * `repo.addActivity` 和 `repo.updateActivity` 已與 `Trip.kt` 不相容。
     * 暫時拋出 NotImplementedError 以修正編譯錯誤。
     */
    fun submit() = viewModelScope.launch {
        _state.update { it.copy(submitting = true) }

        // 🔽🔽 ‼️ 暫時禁用此邏輯以修正編譯錯誤 ‼️ 🔽🔽
        _state.update { it.copy(submitting = false, phase = AddActivityUiState.Phase.Error("手動新增/編輯功能尚未支援新的 AI 行程結構")) }
        // 拋出錯誤，讓開發者知道這裡需要重構
        throw NotImplementedError("add/edit activity logic needs refactoring for Slot-based model")

        /*
        // --- 以下是舊的、已損壞的邏輯 ---
       val s = _state.value
       val t = s.trip ?: return@launch
       // ... (舊的日期檢查邏輯)

       val dateStr = millisToDateString(millis)
       val newDayIndex = findDayIndexByDate(t, dateStr)
           ?: return@launch _state.update { it.copy(phase = AddActivityUiState.Phase.Error("日期不在行程範圍內")) }


       _state.update { it.copy(submitting = true) }

       when (val m = mode) {
           Mode.Add -> {
               // ❌ 錯誤：新的 Activity (Place) 沒有 'place', 'startTime', 'endTime' 欄位
               val act = Activity(
                   id = UUID.randomUUID().toString(),
                   place = (s.place ?: placeLiteFromArg)!!.toFull(), // 👈 錯誤
                   startTime = s.startTime, // 👈 錯誤
                   endTime = s.endTime,     // 👈 錯誤
                   note = s.note            // 👈 錯誤
               )
               // ❌ 錯誤：repo.addActivity 試圖寫入 day.activities (已不存在)
               runCatching { repo.addActivity(tripId, newDayIndex, act) }
                   .onSuccess {
                       _state.update { it.copy(submitting = false) }
                       _effects.tryEmit(Effect.NavigateToDetail(tripId))
                   }
                   .onFailure { e ->
                       _state.update { it.copy(submitting = false, phase = AddActivityUiState.Phase.Error(e.message ?: "新增失敗")) }
                   }
           }
           is Mode.Edit -> {
               // ❌ 錯誤：findActivityPositionAndDate 已被修改
               val pos = findActivityPositionAndDate(t, m.activityId)
               if (pos == null) {
                   _state.update { it.copy(submitting = false, phase = AddActivityUiState.Phase.Error("找不到活動")) }
                   return@launch
               }
               val (oldDayIndex, oldActIndex, act0, _) = pos

               // ❌ 錯誤：act0.copy 沒有 'startTime' 等欄位
               val updated = act0.copy(
                   startTime = s.startTime,
                   endTime = s.endTime,
                   note = s.note
               )

               val result = runCatching {
                   if (newDayIndex == oldDayIndex) {
                       repo.updateActivity(tripId, oldDayIndex, oldActIndex, updated)
                   } else {
                       repo.removeActivity(tripId, oldDayIndex, oldActIndex)
                       repo.addActivity(tripId, newDayIndex, updated)
                   }
               }
               result
                   .onSuccess {
                       _state.update { it.copy(submitting = false) }
                       _effects.tryEmit(Effect.NavigateToDetail(tripId))
                   }
                   .onFailure { e ->
                       _state.update { it.copy(submitting = false, phase = AddActivityUiState.Phase.Error(e.message ?: "更新失敗")) }
                   }
           }
       }
       */
        // 🔼🔼
    }

    /** * ✅ 修正：重寫函式以遍歷 slots 和 places
     * 找到 (dayIndex, slotIndex, activityIndex, Activity, Slot, DaySchedule)
     */
    private fun findActivityLocation(trip: Trip, activityId: String)
            : ActivityFullLocation? { // 👈 使用新的 Helper data class
        trip.days.forEachIndexed { dIdx, day ->
            day.slots.forEachIndexed { sIdx, slot ->
                val aIdx = slot.places.indexOfFirst { it.id == activityId }
                if (aIdx >= 0) {
                    val act = slot.places[aIdx]
                    return ActivityFullLocation(dIdx, sIdx, aIdx, act, slot, day)
                }
            }
        }
        return null
    }

    // 🔽🔽 ‼️ 刪除舊的、報錯的 findActivityPositionAndDate 函式 ‼️ 🔽🔽
    /*
    private fun findActivityPositionAndDate(trip: Trip, activityId: String)
            : Quadruple<Int, Int, Activity, LocalDate>? {
        trip.days.forEachIndexed { dIdx, day ->
            val aIdx = day.activities.indexOfFirst { it.id == activityId } // 👈 錯誤
            if (aIdx >= 0) {
                val act = day.activities[aIdx] // 👈 錯誤
                val dayDate = when (val d = day.date) {
                    is String -> LocalDate.parse(d, DATE_FMT)
                    is java.time.LocalDate -> d
                    is java.time.LocalDateTime -> d.toLocalDate()
                    else -> LocalDate.parse(d.toString(), DATE_FMT)
                }
                return Quadruple(dIdx, aIdx, act, dayDate) // 👈 錯誤
            }
        }
        return null
    }
    */
    // 🔼🔼
}

/** * ✅ 新增：輔助 data class，用於儲存 Activity 的完整上下文
 */
private data class ActivityFullLocation(
    val dayIndex: Int,
    val slotIndex: Int,
    val activityIndex: Int,
    val activity: Activity,
    val slot: Slot,     // 👈 確保 import
    val day: DaySchedule // 👈 確保 import
)

// 🔽🔽 ‼️ 刪除舊的 Quadruple ‼️ 🔽🔽
// data class Quadruple<A,B,C,D>(val first: A, val second: B, val third: C, val fourth: D)
// 🔼🔼


/**
 * 🔽🔽 ‼️ 你需要提供這兩個 'toLite' / 'toFull' 轉接器 ‼️ 🔽🔽
 * (你可能需要把這兩個函式移到它們各自的 data class 檔案中，例如 Place.kt 或 Trip.kt)
 */
/**
 * 輔助函式：將*新*的 Activity (Place-like) 物件轉換為 PlaceLite
 * (PlaceLite 似乎是你的 UI 選擇器模型)
 */
fun Activity.toPlaceLite(): PlaceLite {
    return PlaceLite(
        placeId = this.id,
        name = this.name,
        lat = this.lat,
        lng = this.lng,
        address = this.address,
        rating = this.rating,
        userRatingsTotal = this.reviews,
        photoUrl = this.photoUrl,
        openingHours = this.openingHours,
        openNow = this.openNow,
        openStatusText = this.openStatusText
    )
}

/**
 * 輔助函式：將 PlaceLite 轉換為*新*的 Activity
 * (注意：toFull() 之前回傳的是舊的 Place，現在我們讓它回傳新的 Activity)
 */
fun PlaceLite.toFullActivity(): Activity {
    // 建立一個*新*的 Activity 物件
    return Activity(
        id = this.placeId,
        name = this.name,
        lat = this.lat,
        lng = this.lng,
        address = this.address,
        rating = this.rating,
        reviews = this.userRatingsTotal
        // (其他欄位使用 Activity data class 中的預設值)
    )
}
// 🔼🔼