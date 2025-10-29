package com.example.thelastone.data.remote

import com.example.thelastone.data.model.Alternative
import com.example.thelastone.data.model.StartInfo // 👈 1. 新增 Import
import com.example.thelastone.data.model.Trip
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// (GeneralRecommendationResponse - 保持不變)
@Serializable
data class GeneralRecommendationResponse(
    val top3: List<Trip>,
    val more: List<Trip>,
    val user_id: String,
    val generated_at: String
)

// (ReplaceActivityRequest - 保持不變)
@Serializable
data class ReplaceActivityRequest(
    val old_activity_id: String,
    val new_activity_data: Alternative
)

// 🔽🔽 2. 將 Request Body DTOs 移到這裡 🔽🔽
// (這些欄位需要跟你 FastAPI 端點接收的 Pydantic 模型完全對應)

/**
 * 獲取景點初始資訊的請求內文
 */
@Serializable
data class StartInfoRequest(
    val placeId: String,
    val lat: Double,
    val lng: Double
    // ... 其他需要的欄位 ...
)

/**
 * 獲取替代景點的請求內文
 */
@Serializable
data class AlternativesRequest(
    val current_place_id: String,
    val page: Int
    // val lat: Double? = null, // 根據需要加入
    // val lng: Double? = null,
    // val weather: String? = null,
    // val user_id: String? = null,
    // val radius_meters: Int? = 5000,
    // val max_results: Int? = 10
)
// 🔼🔼 結束移動 🔼🔼


interface ApiService {

    // (getRecommendations - 保持不變)
    @POST("/")
    suspend fun getRecommendations(
        @Body requestWrapper: ApiRecommendRequest
    ): Trip

    // (getGeneralRecommendations - 保持不變)
    @GET("api/recommendations")
    suspend fun getGeneralRecommendations(
        @Query("top_k") topK: Int = 5,
        @Query("more_k") moreK: Int = 10
    ): GeneralRecommendationResponse

    // (replaceActivity - 保持不變)
    @POST("trips/{tripId}/replace")
    suspend fun replaceActivity(
        @Path("tripId") tripId: String,
        @Body request: ReplaceActivityRequest
    ): Trip

    // 🔽🔽 3. 新增 Start 流程需要的 API 函式 🔽🔽

    /**
     * 取得景點的即時資訊 (天氣、營業狀態、初步替代方案)
     * ‼️ 注意：請將 "startInfo" 替換成您後端 FastAPI 的正確路徑！
     */
    @POST("startInfo") // 👈 假設的路徑，請依後端為準
    suspend fun getStartInfo(
        @Body request: StartInfoRequest
    ): StartInfo

    /**
     * 取得更多替代景點 (分頁)
     * ‼️ 注意：請將 "alternatives" 替換成您後端 FastAPI 的正確路徑！
     */
    @POST("alternatives") // 👈 假設的路徑，請依後端為準
    suspend fun getAlternatives(
        @Body request: AlternativesRequest
    ): List<Alternative> // 假設 API 會回傳 Alternative 列表

    // 🔼🔼 結束新增 🔼🔼
}
