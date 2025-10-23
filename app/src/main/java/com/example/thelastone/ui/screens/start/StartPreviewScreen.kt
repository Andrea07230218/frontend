// 檔案路徑：ui/screens/start/StartPreviewScreen.kt
package com.example.thelastone.ui.screens.start

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thelastone.vm.GenerationState // 👈 1. Import
import com.example.thelastone.vm.NavigationEffect // 👈 2. Import
import com.example.thelastone.vm.StartPreviewViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StartPreviewScreen(
    viewModel: StartPreviewViewModel = hiltViewModel(),
    onNavigateToTripDetail: (String) -> Unit // 假設由外部傳入導航 lambda
) {
    val uiState by viewModel.state.collectAsState()
    val generationState = uiState.generationState // 取得 AI 生成的狀態

    // 處理一次性的導航指令
    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collectLatest { effect ->
            when (effect) {
                is NavigationEffect.NavigateToTripDetail -> {
                    onNavigateToTripDetail(effect.tripId)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.loading) {
            CircularProgressIndicator()
        } else if (uiState.form != null) {
            val form = uiState.form!!
            Text(text = "即將生成的行程：${form.name}")
            Text(text = "地點：${form.locations}")

            // (你 UI 上顯示表單詳情... )

            // 根據 AI 生成狀態顯示不同 UI
            when (generationState) {
                is GenerationState.Idle -> {
                    // (顯示 "Generate" 按鈕)
                    Button(onClick = {
                        // 假設 "user_123" 是登入用戶, "" 是排除條件
                        viewModel.generateItinerary("user_123", "")
                    }) {
                        Text("開始生成")
                    }
                }
                is GenerationState.Loading -> {
                    CircularProgressIndicator()
                    Text("AI 正在生成行程...")
                }
                is GenerationState.Success -> {
                    // ✅ 3. 這裡就是修正的地方
                    // 錯誤的舊程式碼： val tripName = generationState.response.tripName
                    // 正確的新程式碼：
                    val tripName = generationState.trip.name

                    Text("✅ 生成成功！")
                    Text("行程名稱：$tripName")

                    // (顯示 "儲存" 按鈕)
                    Button(onClick = { viewModel.onConfirmAndSave() }) {
                        Text("儲存並完成")
                    }
                }
                is GenerationState.Error -> {
                    Text("❌ 發生錯誤：${generationState.message}")
                }
            }

        } else if (uiState.error != null) {
            Text(text = "錯誤：${uiState.error}")
        }
    }
}