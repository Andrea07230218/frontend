// 檔案路徑：data/repo/impl/TripRepositoryImpl.kt
package com.example.thelastone.data.repo.impl

import com.example.thelastone.data.local.TripDao
import com.example.thelastone.data.mapper.toApiRequestForm
import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.data.remote.ApiRecommendRequest
import com.example.thelastone.data.remote.ApiService
import com.example.thelastone.data.remote.RecommendationForm
import com.example.thelastone.data.repo.TripRepository
import com.example.thelastone.data.repo.TripStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf // 👈 Import for empty flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tripDao: TripDao
) : TripRepository {

    private var formForPreview: TripForm? = null

    /**
     * ✅ 已修正簽名：加入 userId 參數
     */
    override suspend fun createTrip(form: TripForm, userId: String): Trip {
        return withContext(Dispatchers.IO) {
            val apiForm: RecommendationForm = form.toApiRequestForm(excludeTerms = emptyList())
            val requestWrapper = ApiRecommendRequest(userId = userId, form = apiForm)
            apiService.getRecommendations(requestWrapper)
        }
    }

    override suspend fun saveTrip(trip: Trip): Trip {
        withContext(Dispatchers.IO) { tripDao.insertTrip(trip) }
        return trip // 正常回傳
    }

    override fun observeTripDetail(tripId: String): Flow<Trip> {
        return tripDao.observeTripDetail(tripId).filterNotNull() // 正常回傳
    }

    override fun setTripFormForPreview(form: TripForm) {
        this.formForPreview = form // 正常實作
    }

    override fun getTripFormForPreview(): TripForm? {
        return this.formForPreview // 正常回傳
    }

    // --- 為 TODO 函式加上明確拋出錯誤或回傳預設值 ---
    override suspend fun getMyTrips(): List<Trip> {
        // TODO("Not yet implemented")
        throw NotImplementedError("getMyTrips Not yet implemented") // 拋出錯誤
        // 或者 return emptyList() // 回傳空列表
    }

    override fun observeMyTrips(): Flow<List<Trip>> {
        // TODO("Not yet implemented")
        // throw NotImplementedError("observeMyTrips Not yet implemented")
        return flowOf(emptyList()) // 回傳空的 Flow
    }

    override suspend fun getPublicTrips(): List<Trip> {
        return withContext(Dispatchers.IO) {
            try {
                // val publicTrips = apiService.getPublicTrips() // Assume API exists
                val publicTrips = emptyList<Trip>()
                publicTrips.forEach { tripDao.insertTrip(it) }
                publicTrips
            } catch (e: Exception) {
                emptyList() // 回傳空列表
            }
        }
    }

    override fun observePublicTrips(): Flow<List<Trip>> {
        return tripDao.observePublicTrips() // 正常回傳
    }

    override suspend fun getTripDetail(tripId: String): Trip {
        // TODO("Not yet implemented")
        throw NotImplementedError("getTripDetail Not yet implemented")
    }

    override suspend fun addActivity(tripId: String, dayIndex: Int, activity: Activity) {
        TODO("Not yet implemented") // 沒有回傳值，TODO 可以保留
    }

    override suspend fun updateActivity(tripId: String, dayIndex: Int, activityIndex: Int, updated: Activity) {
        TODO("Not yet implemented") // 沒有回傳值，TODO 可以保留
    }

    override suspend fun removeActivity(tripId: String, dayIndex: Int, activityIndex: Int) {
        TODO("Not yet implemented") // 沒有回傳值，TODO 可以保留
    }

    override suspend fun deleteTrip(tripId: String) {
        TODO("Not yet implemented") // 沒有回傳值，TODO 可以保留
    }

    override suspend fun addMembers(tripId: String, userIds: List<String>) {
        TODO("Not yet implemented") // 沒有回傳值，TODO 可以保留
    }

    override suspend fun getTripStatsFor(userId: String): TripStats {
        // TODO("Not yet implemented")
        throw NotImplementedError("getTripStatsFor Not yet implemented")
        // 或者 return TripStats(0, 0) // 回傳預設值
    }
}