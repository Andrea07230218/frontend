package com.example.thelastone.data.repo

import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
import kotlinx.coroutines.flow.Flow

// 🔽 [ [ [ 關鍵修正：TripStats 的定義必須在這裡 ] ] ] 🔽
data class TripStats(
    val created: Int,
    val participating: Int
)

interface TripRepository {
    suspend fun createTrip(form: TripForm): Trip
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
    suspend fun deleteTrip(tripId: String)
    suspend fun addMembers(tripId: String, userIds: List<String>)

    suspend fun getTripStatsFor(userId: String): TripStats

    /**
     * 在導航到預覽畫面之前，暫時儲存使用者填寫的表單。
     */
    fun setTripFormForPreview(form: TripForm)

    /**
     * 從預覽畫面讀取之前暫存的表單。
     */
    fun getTripFormForPreview(): TripForm?
}

