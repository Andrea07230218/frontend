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
            // ‼️ 假設 apiService.getRecommendations 是舊的 POST API ‼️
            // 如果你的 ApiService.kt 中 getRecommendations 被改名了，請用正確的名稱
            apiService.getRecommendations(requestWrapper)
        }
    }

    override suspend fun saveTrip(trip: Trip): Trip {
        // Log 儲存前的 createdBy
        println("TripRepositoryImpl: Saving trip ${trip.id} created by ${trip.createdBy}")
        withContext(Dispatchers.IO) { tripDao.insertTrip(trip) }
        return trip
    }

    override fun observeTripDetail(tripId: String): Flow<Trip> {
        // ‼️ 注意：這個觀察的是 Room DB，可能沒有來自 API 的最新資料 ‼️
        // 你的 TripDao.kt 有這個函式
        return tripDao.observeTripDetail(tripId).filterNotNull()
    }

    override fun setTripFormForPreview(form: TripForm) {
        this.formForPreview = form
    }

    override fun getTripFormForPreview(): TripForm? {
        return this.formForPreview
    }

    override suspend fun getMyTrips(): List<Trip> {
        // 實際應用中，這裡可能需要區分 user ID
        // return withContext(Dispatchers.IO) { tripDao.getAllTripsBlocking() } // 假設 DAO 有這個
        throw NotImplementedError("getMyTrips Not yet implemented")
    }

    // --- 🔽🔽 【修正這裡】 🔽🔽 ---
    override fun observeMyTrips(): Flow<List<Trip>> {
        // 直接回傳 DAO 的觀察 Flow
        // 你的 TripDao.kt 已經有 observeMyTrips()
        println("TripRepositoryImpl: observeMyTrips called, returning Flow from DAO")
        return tripDao.observeMyTrips() // <--- 修正：呼叫 DAO
        // return flowOf(emptyList()) // 移除舊的空 Flow
    }
    // --- 🔼🔼 【結束修正】 🔼🔼 ---


    override suspend fun getPublicTrips(): List<Trip> {
        return withContext(Dispatchers.IO) {
            try {
                // 這裡可能需要呼叫一個取得 public trips 的 API
                // val publicTrips = apiService.getPublicTrips()
                val publicTrips = emptyList<Trip>() // 暫時回傳空
                // 如果從 API 取得，考慮是否要存入 Room
                // publicTrips.forEach { tripDao.insertTrip(it) }
                publicTrips
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override fun observePublicTrips(): Flow<List<Trip>> {
        // 這個是給 Explore 頁面用的 (如果 Explore 讀 Room 的話，但我們改用 API 了)
        // 你的 TripDao.kt 有這個函式
        return tripDao.observePublicTrips()
    }

    override suspend fun getTripDetail(tripId: String): Trip {
        // ‼️ 這個函式只讀取 Room DB，可能讀不到 Explore 頁來的行程 ‼️
        // 這可能是你問題 2 的根源
        return withContext(Dispatchers.IO) {
            // 你的 TripDao.kt 有 getTripByIdBlocking
            tripDao.getTripByIdBlocking(tripId)
                ?: throw NoSuchElementException("Trip not found in local DB: $tripId")
        }
        // throw NotImplementedError("getTripDetail Not yet implemented")
    }

    // --- 其他 TODO 函式 ---
    override suspend fun addActivity(tripId: String, dayIndex: Int, activity: Activity) { TODO("Not yet implemented") }
    override suspend fun updateActivity(tripId: String, dayIndex: Int, activityIndex: Int, updated: Activity) { TODO("Not yet implemented") }
    override suspend fun removeActivity(tripId: String, dayIndex: Int, activityIndex: Int) { TODO("Not yet implemented") }
    override suspend fun deleteTrip(tripId: String) { TODO("Not yet implemented") }
    override suspend fun addMembers(tripId: String, userIds: List<String>) { TODO("Not yet implemented") }
    override suspend fun getTripStatsFor(userId: String): TripStats { TODO("Not yet implemented"); return TripStats(0, 0) }

    // --- 我們之前為 Explore 新增的函式 (保持不變) ---
    override suspend fun fetchGeneralRecommendations(): List<Trip> {
        return withContext(Dispatchers.IO) {
            try {
                // 假設 apiService.getGeneralRecommendations 是我們新增的 GET API
                val response = apiService.getGeneralRecommendations(topK = 5, moreK = 10)
                return@withContext response.top3 + response.more
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
