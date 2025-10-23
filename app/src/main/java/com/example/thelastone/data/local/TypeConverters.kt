// 檔案路徑：data/local/Converters.kt (你也可以命名為 TypeConverters.kt)
package com.example.thelastone.data.local

import androidx.room.TypeConverter
import com.example.thelastone.data.model.* // 👈 Import 你所有的 model
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    // 讓 Json 知道如何處理 Place (如果它是 interface/sealed class)
    // private val module = SerializersModule { ... }
    // private val json = Json { serializersModule = module; ignoreUnknownKeys = true }

    // 簡化版：如果你的所有 model (Place, User) 都只是 data class，用這個就行
    private val json = Json { ignoreUnknownKeys = true }

    // --- List<DaySchedule> ---
    @TypeConverter
    fun fromDayScheduleList(value: List<DaySchedule>): String {
        return json.encodeToString(value)
    }
    @TypeConverter
    fun toDayScheduleList(value: String): List<DaySchedule> {
        return json.decodeFromString(value)
    }

    // --- List<User> ---
    @TypeConverter
    fun fromUserList(value: List<User>): String {
        return json.encodeToString(value)
    }
    @TypeConverter
    fun toUserList(value: String): List<User> {
        return json.decodeFromString(value)
    }

    // --- List<String> (用於 transportPreferences, styles) ---
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    // --- AgeBand (Enum) ---
    @TypeConverter
    fun fromAgeBand(value: AgeBand): String {
        return value.name // 儲存 Enum 的名字 (例如 "ADULT")
    }
    @TypeConverter
    fun toAgeBand(value: String): AgeBand {
        return AgeBand.valueOf(value) // 從 String 讀回 Enum
    }

    // --- TripVisibility (Enum) ---
    @TypeConverter
    fun fromTripVisibility(value: TripVisibility): String {
        return value.name
    }
    @TypeConverter
    fun toTripVisibility(value: String): TripVisibility {
        return TripVisibility.valueOf(value)
    }
}