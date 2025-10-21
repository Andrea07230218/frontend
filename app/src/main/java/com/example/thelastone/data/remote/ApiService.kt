package com.example.thelastone.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    /**
     * 提交表單以生成 AI 推薦行程。
     * Retrofit 會將您在 RecommendModule 中設定的 Base URL ("http://10.0.2.2:8000/")
     * 和這裡的路徑 ("recommend") 組合在一起，
     * 形成最終的請求 URL：http://10.0.2.2:8000/recommend
     */
    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequest
    ): RecommendationResponse
}

