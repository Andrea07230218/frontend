package com.example.thelastone.data.mapper

import com.example.thelastone.data.model.TripForm // 👈 您的 UI 表單
import com.example.thelastone.data.remote.RecommendationForm // 👈 伺服器要的表單
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 將 App 的 UI 表單 (TripForm) 轉換為 API 看得懂的表單 (RecommendationForm)
 */
fun TripForm.toApiRequestForm(): RecommendationForm {

    // 1. [假設] 從開始/結束日期計算天數
    val days = try {
        val start = LocalDate.parse(this.startDate)
        val end = LocalDate.parse(this.endDate)
        ChronoUnit.DAYS.between(start, end).toInt() + 1
    } catch (e: Exception) {
        1 // 預設 1 天
    }

    // 2. [假設] 'styles' 欄位對應到 'preferences'
    val preferences = this.styles
    // 您也可以在這裡加入更多偏好, e.g.:
    // val allPreferences = this.styles + "年齡層:${this.avgAge}"

    // 3. [假設] 'transportPreferences' 第一個值對應到 'transportation'
    val transport = this.transportPreferences.firstOrNull() ?: "public"

    // 4. [假設] 'name' 欄位對應到 'locations'
    // ❗ [重要] 您的 TripForm.kt "沒有" locations 欄位！
    // ❗ 伺服器需要 locations。我先假設 name 就是地點，您必須修改這裡！
    val locations = this.name.split("、", ",", " ")

    // 5. [假設] 'extraNote' 對應到 'notes'
    val notes = this.extraNote

    // 6. 組合
    return RecommendationForm(
        locations = locations,       // ❗ 您必須提供這個欄位
        days = days,
        preferences = preferences,
        exclude = emptyList(),     // ❗ 您的 TripForm 沒有 "exclude"
        transportation = transport,
        notes = notes
    )
}