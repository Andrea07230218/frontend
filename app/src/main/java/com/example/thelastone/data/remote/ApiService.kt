package com.example.thelastone.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequest
    ): RecommendationResponse
}