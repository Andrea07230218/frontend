// 檔案路徑：data/model/Trip.kt
package com.example.thelastone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// --- 重要的 import ---
// 確保您有 import 這三個標註
// 
// 此外，請確保 AgeBand, User, Place 這些外部 model
// 也都在它們各自的檔案中被加上了 @Serializable 標註！
//
// import com.example.thelastone.data.model.AgeBand // (範例)
// import com.example.thelastone.data.model.User // (範例)
// import com.example.thelastone.data.model.Place // (範例)
// --- End Imports ---


@Serializable // 👈 加上 @Serializable，用於 Enum
enum class TripVisibility { PUBLIC, PRIVATE }

@Serializable // 👈 加上 @Serializable (用於 API 和 TypeConverter)
@Entity(tableName = "trips") // 👈 加上 @Entity (用於 Room)
data class Trip(
    @PrimaryKey // 👈 告訴 Room 這是主鍵
    val id: String,
    val createdBy: String,
    val name: String,
    val locations: String,
    val totalBudget: Int?,
    val startDate: String,
    val endDate: String,
    val activityStart: String?,
    val activityEnd: String?,
    val avgAge: AgeBand, // 👈 依賴 AgeBand 也有 @Serializable
    val transportPreferences: List<String>,
    val useGmapsRating: Boolean,
    val styles: List<String>,
    val visibility: TripVisibility = TripVisibility.PRIVATE,
    val members: List<User> = emptyList(), // 👈 依賴 User 也有 @Serializable
    val days: List<DaySchedule> = emptyList() // 👈 依賴 DaySchedule 也有 @Serializable
)

@Serializable // 👈 加上 @Serializable (用於 TypeConverter)
data class DaySchedule(
    val date: String,
    val activities: List<Activity> = emptyList()
)

@Serializable // 👈 加上 @Serializable (用於 TypeConverter)
data class Activity(
    val id: String,
    val place: Place, // 👈 依賴 Place 也有 @Serializable
    val startTime: String? = null,  // "09:00"
    val endTime: String? = null,    // "11:30"
    val note: String? = null
)

// 您 GitHub 中的輔助函式，保持不變
fun Trip.coverPhotoUrl(): String? {
    for (day in days) {
        for (act in day.activities) {
            val url = act.place.photoUrl
            if (!url.isNullOrBlank()) return url
        }
    }
    return null
}