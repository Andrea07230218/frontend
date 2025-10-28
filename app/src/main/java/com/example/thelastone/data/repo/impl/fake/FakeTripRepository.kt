package com.example.thelastone.data.repo.impl.fake // 👈 根據你的資訊，package 是這個

import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.AgeBand
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.model.TripVisibility // 👈 補上 Import
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.data.repo.TripStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTripRepository @Inject constructor() : TripRepository {

    private var formForPreview: TripForm? = null

    /**
     * ✅ 已修正簽名：加入 userId 參數
     */
    override suspend fun createTrip(form: TripForm, userId: String): Trip {
        delay(1500)
        // 使用 form 中的資料來建立假 Trip
        return Trip(
            id = "fake_${System.currentTimeMillis()}",
            createdBy = userId,
            name = form.name.ifBlank { "未命名假行程" },
            locations = form.locations,
            totalBudget = form.totalBudget,
            startDate = form.startDate,
            endDate = form.endDate,
            activityStart = form.activityStart,
            activityEnd = form.activityEnd,
            avgAge = form.avgAge,
            transportPreferences = form.transportPreferences,
            useGmapsRating = form.useGmapsRating,
            styles = form.styles,
            visibility = form.visibility,
            members = emptyList(), // 假資料
            days = emptyList()     // 假資料
        )
    }

    override suspend fun saveTrip(trip: Trip): Trip {
        delay(500)
        println("FakeTripRepository: Pretending to save trip ${trip.id}")
        return trip // 回傳 trip
    }

    override suspend fun getMyTrips(): List<Trip> {
        delay(1000)
        return emptyList() // 回傳空列表
    }

    override fun observeMyTrips(): Flow<List<Trip>> {
        return flowOf(emptyList()) // 回傳空的 Flow
    }

    override suspend fun getPublicTrips(): List<Trip> {
        delay(1000)
        return emptyList() // 回傳空列表
    }

    override fun observePublicTrips(): Flow<List<Trip>> {
        return flowOf(emptyList()) // 回傳空的 Flow
    }

    // 輔助函式，產生一個基礎的假 Trip
    private fun createFakeTrip(tripId: String): Trip {
        return Trip(
            id = tripId, createdBy = "fake_user", name = "假行程 $tripId",
            locations = "假地點", startDate = "2025-01-01", endDate = "2025-01-02",
            avgAge = AgeBand.A26_35, transportPreferences = listOf("WALKING"),
            useGmapsRating = false, styles = listOf("FOODIE"), totalBudget = 1000,
            activityStart = "09:00", activityEnd = "21:00",
            visibility = TripVisibility.PRIVATE, // 使用 import 的 TripVisibility
            members = emptyList(), days = emptyList()
        )
    }

    override suspend fun getTripDetail(tripId: String): Trip {
        delay(500)
        return createFakeTrip(tripId) // 回傳假 Trip
    }

    override fun observeTripDetail(tripId: String): Flow<Trip> {
        return flowOf(createFakeTrip(tripId)) // 回傳包含假 Trip 的 Flow
    }

    // --- 不需要回傳值的函式 ---
    override suspend fun addActivity(tripId: String, dayIndex: Int, activity: Activity) { delay(100) }
    override suspend fun updateActivity(tripId: String, dayIndex: Int, activityIndex: Int, updated: Activity) { delay(100) }
    override suspend fun removeActivity(tripId: String, dayIndex: Int, activityIndex: Int) { delay(100) }
    override suspend fun deleteTrip(tripId: String) { delay(500) }
    override suspend fun addMembers(tripId: String, userIds: List<String>) { delay(200) }

    // --- 需要回傳值的函式 ---
    override suspend fun getTripStatsFor(userId: String): TripStats {
        delay(300)
        return TripStats(created = 5, participating = 2) // 回傳假資料
    }

    override fun setTripFormForPreview(form: TripForm) {
        this.formForPreview = form
    }

    override fun getTripFormForPreview(): TripForm? {
        return this.formForPreview
    }

    // 🔽🔽 【這就是缺少的函式】 🔽🔽
    /**
     * 呼叫 API 取得「通用」推薦行程 (給 Explore 頁用)
     */
    override suspend fun fetchGeneralRecommendations(): List<Trip> {
        delay(1000) // 模擬 API 呼叫延遲
        // 為了讓測試 UI 時能看到東西，回傳幾個假行程
        return listOf(
            createFakeTrip("fake-general-1"),
            createFakeTrip("fake-general-2"),
            createFakeTrip("fake-general-3")
        )
    }
}
