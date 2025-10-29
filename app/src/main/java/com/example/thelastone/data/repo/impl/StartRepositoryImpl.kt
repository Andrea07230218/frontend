package com.example.thelastone.data.repo.impl

import com.example.thelastone.data.model.*
import com.example.thelastone.data.remote.ApiService
import com.example.thelastone.data.remote.AlternativesRequest // 👈 1. 新增 Import
import com.example.thelastone.data.remote.StartInfoRequest // 👈 1. 新增 Import
import com.example.thelastone.data.repo.StartRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StartRepository 的真正實作，負責呼叫後端 API。
 */
@Singleton
class StartRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : StartRepository {

    /**
     * 呼叫後端 API 取得天氣、營業狀態和初步替代方案。
     */
    override suspend fun getStartInfo(place: Place): StartInfo {
        // 建立要傳送給後端的請求資料
        val requestBody = StartInfoRequest(
            placeId = place.placeId,
            lat = place.lat,
            lng = place.lng
        )

        // 🔽🔽 2. 啟用真正的 API 呼叫 🔽🔽
        return apiService.getStartInfo(requestBody)
        // 🔼🔼

        // --- 暫時回傳假資料 (已移除) ---
    }

    /**
     * 呼叫後端 API 取得更多替代景點。
     */
    override suspend fun getAlternatives(placeId: String, page: Int): List<Alternative> {
        // 建立要傳送給後端的請求資料
        val requestBody = AlternativesRequest(
            current_place_id = placeId,
            page = page
            // 根據需要加入其他欄位
        )

        // 🔽🔽 3. 啟用真正的 API 呼叫 🔽🔽
        return apiService.getAlternatives(requestBody)
        // 🔼🔼

        // --- 暫時回傳假資料 (已移除) ---
    }
}

// 🔽🔽 4. 移除這裡的資料類別定義 (已移至 ApiService.kt) 🔽🔽
// (StartInfoRequest 和 AlternativesRequest 已被移走)
