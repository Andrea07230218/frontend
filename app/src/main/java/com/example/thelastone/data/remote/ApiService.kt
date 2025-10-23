// 檔案路徑：data/remote/ApiService.kt
package com.example.thelastone.data.remote

import com.example.thelastone.data.model.Trip
// 🔽🔽 1. 確認 Import 的是 ApiRecommendRequest (Wrapper) 🔽🔽
import com.example.thelastone.data.remote.ApiRecommendRequest
// 🔼🔼
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("recommend")
    suspend fun getRecommendations(
        // 2. ✅ 修正 @Body 參數型別為 ApiRecommendRequest
        @Body requestWrapper: ApiRecommendRequest
    ): Trip // (回傳 Trip 是正確的)
}