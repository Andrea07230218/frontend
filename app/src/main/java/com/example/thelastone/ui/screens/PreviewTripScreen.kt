// 檔案路徑：ui/screens/PreviewTripScreen.kt
package com.example.thelastone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.thelastone.ui.state.ErrorState
import com.example.thelastone.ui.state.LoadingState
import com.example.thelastone.vm.TripFormViewModel

@Composable
fun PreviewTripScreen(
    padding: PaddingValues,
    viewModel: TripFormViewModel,
    onConfirmSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val preview by viewModel.preview.collectAsState()
    val save by viewModel.save.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(save) {
        when (save) {
            is TripFormViewModel.SaveUiState.Success -> {
                val id = (save as TripFormViewModel.SaveUiState.Success).tripId
                viewModel.resetSaveState()
                onConfirmSaved(id)
            }
            is TripFormViewModel.SaveUiState.Error -> {
                Toast.makeText(context, (save as TripFormViewModel.SaveUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    when (val p = preview) {
        TripFormViewModel.PreviewUiState.Idle,
        TripFormViewModel.PreviewUiState.Loading -> {
            // ✅ 修正：Idle 狀態不應該顯示 Loading
            // 這裡假設 ViewModel 在呼叫 generatePreview 時會切換到 Loading
            if (p is TripFormViewModel.PreviewUiState.Loading) {
                LoadingState(modifier = Modifier.padding(padding))
            } else {
                // Idle 狀態，可能顯示一個提示或保持空白
                // LoadingState(modifier = Modifier.padding(padding)) // 暫時保留
            }
        }
        is TripFormViewModel.PreviewUiState.Error -> {
            ErrorState(
                modifier = Modifier.padding(padding),
                title = "預覽失敗",
                message = p.message,
                retryLabel = "返回",
                onRetry = onBack
            )
        }
        is TripFormViewModel.PreviewUiState.Data -> {
            val trip = p.trip
            var selected by rememberSaveable { mutableIntStateOf(0) }

            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // 內容區
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { TripInfoCard(trip) } // 👈 標頭 (已正確顯示)

                    // 🔽🔽 ‼️ 修正 (1)：`onActivityClick` 接收 4 個參數 ‼️ 🔽🔽
                    dayTabsAndActivities(
                        trip = trip,
                        selected = selected,
                        onSelect = { selected = it },
                        onActivityClick = { dayIdx, slotIdx, actIdx, act -> // 👈 接收 4 個參數
                            // 🔽🔽 ‼️ 修正 (2)：使用 `act.name` 而不是 `act.place.name` ‼️ 🔽🔽
                            Toast.makeText(context, "預覽中：${act.name}", Toast.LENGTH_SHORT).show()
                            // 🔼🔼
                        }
                    )
                    // 🔼🔼

                    item { Spacer(Modifier.height(8.dp)) }
                }

                // 底部動作列
                val saving = save is TripFormViewModel.SaveUiState.Loading
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) { Text("返回修改") }

                    Button(
                        onClick = { viewModel.confirmSave() },
                        enabled = !saving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("確認儲存")
                    }
                }
            }
        }
    }
}