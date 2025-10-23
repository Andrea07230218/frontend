package com.example.thelastone.data.model

import kotlinx.serialization.Serializable

@Serializable // 👈 你已經加了
enum class AgeBand {
    IGNORE, UNDER_17, A18_25, A26_35, A36_45, A46_55, A56_PLUS
}

@Serializable // 👈 ‼️ 請加上這一行 ‼️
data class TripForm(
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
    val aiDisclaimerChecked: Boolean = false
)