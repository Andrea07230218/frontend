// 檔案路徑：data/mapper/TripFormMapper.kt
package com.example.thelastone.data.mapper

import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.remote.RecommendationForm // 👈 確保 import 正確
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 將 App 的 UI 表單 (TripForm) 轉換為 API 看得懂的表單 (RecommendationForm)
 * ✅ 已更新：加入所有遺失的欄位
 */
fun TripForm.toApiRequestForm(excludeTerms: List<String>): RecommendationForm {

    // (你原本的 "days" 和 "locationsList" 邏輯保持不變)
    val days = try {
        val start = LocalDate.parse(this.startDate)
        val end = LocalDate.parse(this.endDate)
        ChronoUnit.DAYS.between(start, end).toInt() + 1
    } catch (e: Exception) {
        1 // 預設 1 天
    }
    val locationsList = this.locations.split(Regex("[,、，\\s]+"))
        .filter { it.isNotBlank() }
    val transport = this.transportPreferences.firstOrNull() ?: "public"

    // 組合
    return RecommendationForm(
        // --- 原本的欄位 ---
        locations = locationsList,
        days = days,
        preferences = this.styles, // 'styles' 欄位直接對應到 'preferences'
        exclude = excludeTerms,    // 使用從預覽畫面傳入的排除條件
        transportation = transport,
        notes = this.extraNote,

        // --- 🔽🔽 ‼️ 加入遺失欄位的對應 ‼️ 🔽🔽 ---
        tripName = this.name,
        startDate = this.startDate,
        endDate = this.endDate,
        activityStart = this.activityStart,
        activityEnd = this.activityEnd,
        totalBudget = this.totalBudget,
        avgAge = this.avgAge.name, // 👈 將 Enum 轉換為字串 (例如 "A26_35")
        useGmapsRating = this.useGmapsRating,
        visibility = this.visibility.name // 👈 將 Enum 轉換為字串 (例如 "PRIVATE")
    )
}