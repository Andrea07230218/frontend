package com.example.thelastone.data.repo

import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.Alternative // 👈 1. 加入 Import
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
import kotlinx.coroutines.flow.Flow

data class TripStats(
    val created: Int,
    val participating: Int
)

interface TripRepository {
    // 🔽🔽 ‼️ 只保留有 userId 的這一個 ‼️ 🔽🔽
    suspend fun createTrip(form: TripForm, userId: String): Trip
    // suspend fun createTrip(form: TripForm): Trip // 👈 刪除這一行
    // 🔼🔼

    suspend fun saveTrip(trip: Trip): Trip

    suspend fun getMyTrips(): List<Trip>
    fun observeMyTrips(): Flow<List<Trip>>

    suspend fun getPublicTrips(): List<Trip>
    fun observePublicTrips(): Flow<List<Trip>>

    suspend fun getTripDetail(tripId: String): Trip
    fun observeTripDetail(tripId: String): Flow<Trip>
    suspend fun addActivity(tripId: String, dayIndex: Int, activity: Activity)
    suspend fun updateActivity(tripId: String, dayIndex: Int, activityIndex: Int, updated: Activity)
    suspend fun removeActivity(tripId: String, dayIndex: Int, activityIndex: Int)

    // 🔽🔽 2. 【這就是你需要的函式宣告】 🔽🔽
    /**
     * 替換一個行程中的活動 (景點)
     * @param tripId 行程 ID
     * @param oldActivityId 要被換掉的舊活動 ID
     * @param newActivityData 用來替換的新景點資料
     */
    suspend fun replaceActivityInTrip(
        tripId: String,
        oldActivityId: String,
        newActivityData: Alternative
    )
    // 🔼🔼

    suspend fun deleteTrip(tripId: String)
    suspend fun addMembers(tripId: String, userIds: List<String>)

    suspend fun getTripStatsFor(userId: String): TripStats

    fun setTripFormForPreview(form: TripForm)
    fun getTripFormForPreview(): TripForm?

    /**
     * 呼叫 API 取得「通用」推薦行程 (給 Explore 頁用)
     */
    suspend fun fetchGeneralRecommendations(): List<Trip>
}