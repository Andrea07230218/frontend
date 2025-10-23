// 檔案路徑：data/model/Trip.kt
package com.example.thelastone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName // 👈 確保 import
import kotlinx.serialization.Serializable

// 確保 AgeBand, User, Place 這些外部 model 也在各自檔案中被加上 @Serializable
// import com.example.thelastone.data.model.AgeBand // (範例)
// import com.example.thelastone.data.model.User // (範例)
// import com.example.thelastone.data.model.Place // (範例)

@Serializable
enum class TripVisibility { PUBLIC, PRIVATE }

@Serializable
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val createdBy: String,
    val name: String,
    val locations: String,
    val totalBudget: Int?,
    val startDate: String?, // 👈 修正：允許 null
    val endDate: String?,   // 👈 修正：允許 null
    val activityStart: String?,
    val activityEnd: String?,
    val avgAge: AgeBand,
    val transportPreferences: List<String>,
    val useGmapsRating: Boolean,
    val styles: List<String>,
    val visibility: TripVisibility = TripVisibility.PRIVATE,
    val members: List<User> = emptyList(),
    val days: List<DaySchedule> = emptyList() // 👈 這裡的 DaySchedule 已被修正
)

@Serializable
data class DaySchedule(
    @SerialName("date")
    val date: String,

    @SerialName("city")
    val city: String? = null,

    // ✅ 修正：欄位名稱從 "activities" 改為 "slots"
    @SerialName("slots")
    val slots: List<Slot> = emptyList()
)

/**
 * 新增：用來匹配 API 回傳的 "slots" 結構
 */
@Serializable
data class Slot(
    @SerialName("label")
    val label: String, // "上午", "中午"

    @SerialName("window")
    val window: List<String>, // ["09:00", "12:00"]

    @SerialName("places")
    val places: List<Activity> = emptyList() // Slot 裡面包含 places (即 Activity)
)

/**
 * ✅ 修正：Activity 模型現在完全匹配 API 回傳的 "places" 物件結構
 */
@Serializable
data class Activity(
    @SerialName("place_id")
    val id: String, // 👈 使用 "place_id" 作為 id

    @SerialName("name")
    val name: String,

    @SerialName("category")
    val category: String? = null,

    @SerialName("stay_minutes")
    val stayMinutes: Int? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("reviews")
    val reviews: Int? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("map_url")
    val mapUrl: String? = null,

    @SerialName("open_text")
    val openText: String? = null,

    @SerialName("types")
    val types: List<String> = emptyList(),

    @SerialName("lat")
    val lat: Double,

    @SerialName("lng")
    val lng: Double,

    @SerialName("_from_prev_leg_min")
    val fromPrevLegMin: Int? = null
) {
    // --- 為了向下相容 ActivityBottomSheet 而加入的輔助屬性 ---

    // 你的 API JSON 裡似乎沒有 photoUrl，你需要自己想辦法取得
    val photoUrl: String?
        get() = null

    val userRatingsTotal: Int?
        get() = reviews

    // 簡單模擬 openingHours, openNow, openStatusText
    val openingHours: List<String>
        get() = openText?.let { listOf(it) } ?: emptyList()

    val openNow: Boolean?
        get() = null // 你的 API 沒提供這個欄位

    val openStatusText: String?
        get() = openText

    // 這些欄位現在由 Slot 控制，Activity 本身不再儲存
    val startTime: String? get() = null
    val endTime: String? get() = null
    val note: String? get() = null
}

/**
 * ✅ 修正：coverPhotoUrl 現在遍歷新的 slots -> places 結構
 */
fun Trip.coverPhotoUrl(): String? {
    for (day in days) {
        for (slot in day.slots) {
            for (act in slot.places) {
                val url = act.photoUrl // 👈 photoUrl 現在是 null (除非你修改上面的 get())
                if (!url.isNullOrBlank()) return url
            }
        }
    }
    return null
}