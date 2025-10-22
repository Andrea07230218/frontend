package com.example.thelastone.ui.screens.start

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.thelastone.data.model.TripForm
import com.example.thelastone.ui.screens.comp.MyTopAppBar
import com.example.thelastone.ui.navigation.Root
import com.example.thelastone.ui.navigation.TripRoutes
import com.example.thelastone.ui.state.ErrorState
import com.example.thelastone.ui.state.LoadingState
import com.example.thelastone.vm.GenerationState
import com.example.thelastone.vm.NavigationEffect
import com.example.thelastone.vm.StartPreviewViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StartPreviewScreen(
    onBack: () -> Unit,
    navController: NavController,
    viewModel: StartPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 監聽來自 ViewModel 的一次性導航指令
    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collectLatest { effect ->
            when (effect) {
                is NavigationEffect.NavigateToTripDetail -> {
                    // 執行導航到新行程的細節頁面
                    navController.navigate(TripRoutes.detail(effect.tripId)) {
                        // 從返回堆疊中移除表單和預覽頁面，讓使用者按返回時直接回到主畫面
                        popUpTo(Root.MyTrips.route) {
                            inclusive = false
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { MyTopAppBar(title = "預覽與生成", onBack = onBack) }
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(
                modifier = Modifier.padding(padding),
                message = state.error!!,
                onRetry = onBack,
                retryLabel = "返回表單"
            )
            state.form != null -> {
                val generationState = state.generationState
                when (generationState) {
                    is GenerationState.Loading -> {
                        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("AI 行程生成中，請稍候...")
                            }
                        }
                    }
                    is GenerationState.Success -> {
                        GeneratedItineraryView(
                            htmlContent = generationState.response.itineraryHtml,
                            onConfirm = { viewModel.onConfirmAndSave() }
                        )
                    }
                    is GenerationState.Error -> {
                        ErrorState(
                            modifier = Modifier.padding(padding),
                            message = generationState.message,
                            onRetry = onBack,
                            retryLabel = "返回重試"
                        )
                    }
                    is GenerationState.Idle -> {
                        PreviewAndGenerateContent(
                            padding = padding,
                            form = state.form!!,
                            onGenerate = { excludeTerms ->
                                // ❗ 這裡應傳入真實的 User ID
                                val fakeUserId = "user_123"
                                viewModel.generateItinerary(fakeUserId, excludeTerms)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 顯示表單摘要和「生成」按鈕的初始畫面
 */
@Composable
private fun PreviewAndGenerateContent(
    padding: PaddingValues,
    form: TripForm,
    onGenerate: (String) -> Unit
) {
    var excludeTerms by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            item {
                Text("行程預覽", style = MaterialTheme.typography.headlineSmall)
                FormDetailItem("旅遊地點:", form.locations)
                FormDetailItem("行程名稱:", form.name)
                FormDetailItem("旅遊日期:", "${form.startDate} ~ ${form.endDate}")
                FormDetailItem("旅遊風格:", form.styles.joinToString())
            }

            item {
                OutlinedTextField(
                    value = excludeTerms,
                    onValueChange = { excludeTerms = it },
                    label = { Text("排除條件（選填）") },
                    placeholder = { Text("例如：購物、博物館") },
                    supportingText = { Text("不想去的類型，請用逗號或空格分隔") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = { onGenerate(excludeTerms) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("生成我的 AI 行程")
        }
    }
}

@Composable
private fun FormDetailItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * 顯示 AI 生成的 HTML 結果和「儲存」按鈕的畫面
 */
@Composable
private fun GeneratedItineraryView(htmlContent: String, onConfirm: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("儲存並完成")
        }
    }
}