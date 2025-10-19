package com.example.thelastone.data.model

// 新增：平均年齡分組（單選）
enum class AgeBand {
    IGNORE, UNDER_17, A18_25, A26_35, A36_45, A46_55, A56_PLUS
}

data class TripForm(
    // [新增] 旅遊地點，用字串形式儲存，允許多個地點以逗號分隔
    val locations: String = "",

    val name: String,
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

    // [新增] 排除條件，用字串形式儲存，允許多個條件以逗號分隔
    val exclude: String = ""
)
