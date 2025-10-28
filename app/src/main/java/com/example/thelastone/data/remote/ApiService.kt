package com.example.thelastone.data.remote

// 🔽🔽 1. 移除 Moshi 的 import 🔽🔽
// import com.squareup.moshi.JsonClass (移除這行)

// 🔽🔽 2. 引入 kotlinx.serialization 🔽🔽
import com.example.thelastone.data.model.Trip
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 🔽🔽 3. 在你的 data class 上方加上 @Serializable 註解 🔽🔽
@Serializable
data class GeneralRecommendationResponse(
    val top3: List<Trip>,
    val more: List<Trip>,
    val user_id: String,
    val generated_at: String
)

interface ApiService {

    // ✅ 你原有的 API (保持不變)
    @POST("recommend")
    suspend fun getRecommendations(
        @Body requestWrapper: ApiRecommendRequest
    ): Trip

    // ✅ 我們的通用 API (路徑已修正)
    /**
     * 取得「通用」的推薦行程 (用於探索頁)
     * API: GET /recommend/api/recommendations
     */
    @GET("recommend/api/recommendations") // ‼️ 這是修正後的路徑 ‼️
    suspend fun getGeneralRecommendations(
        @Query("top_k") topK: Int = 5,
        @Query("more_k") moreK: Int = 10
    ): GeneralRecommendationResponse
}