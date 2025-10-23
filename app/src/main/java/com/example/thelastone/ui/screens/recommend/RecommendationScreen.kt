// 檔案路徑：ui/screens/recommend/RecommendationScreen.kt
package com.example.thelastone.ui.screens.recommend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thelastone.data.model.AgeBand // 👈 (請改成你的 實際路徑)
import com.example.thelastone.data.model.TripVisibility // 👈 (請改成你的 實際路徑)

// Import Form 和 State (來自 RecommendationState.kt)
import com.example.thelastone.ui.screens.recommend.RecommendationForm
import com.example.thelastone.ui.screens.recommend.RecommendationUiState

@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ... (你其他的 UI 元件)

        Button(
            onClick = {
                // 🔽🔽 1. 把 userId 加回來 🔽🔽
                val testUserId = "user_123" // 或者從 ViewModel 或其他地方獲取真實 ID
                // 🔼🔼

                // 2. 建立 RecommendationForm (UI 表單)
                val testForm = RecommendationForm(
                    tripName = "我的台南之旅",
                    locations = "台南",
                    totalBudget = null,
                    startDate = "2025-11-20",
                    endDate = "2025-11-22",
                    activityStart = "09:00",
                    activityEnd = "22:00",
                    avgAge = AgeBand.A26_35, // (範例)
                    transportPreferences = listOf("WALKING", "SCOOTER"),
                    styles = listOf("HISTORY", "FOODIE"),
                    useGmapsRating = true,
                    visibility = TripVisibility.PRIVATE,
                    extraNote = null,
                    aiDisclaimerChecked = true
                )

                // 3. ✅ 呼叫 ViewModel 時傳入 userId 和 form
                viewModel.fetchRecommendations(testUserId, testForm)
            },
            enabled = uiState !is RecommendationUiState.Loading
        ) {
            Text(text = "產生推薦行程")
        }

        Spacer(Modifier.height(16.dp))

        // 4. `when` 區塊保持不變
        when (val state = uiState) {
            is RecommendationUiState.Idle -> { /* ... */ }
            is RecommendationUiState.Loading -> { /* ... */ }
            is RecommendationUiState.Success -> { /* ... */ }
            is RecommendationUiState.Error -> { /* ... */ }
        }
    }
}