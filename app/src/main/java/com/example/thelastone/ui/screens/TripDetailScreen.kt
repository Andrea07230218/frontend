// 檔案路徑：ui/screens/TripDetailScreen.kt
package com.example.thelastone.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // 👈 🔽🔽 1. 補上所有 Layout Imports 🔽🔽
import androidx.compose.foundation.layout.width
// 🔼🔼
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat // 👈 2. 補上 'Chat' Import
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.Place    // 👈 3. 確保 Import 舊的 Place 模型
import com.example.thelastone.data.model.Trip
import com.example.thelastone.ui.AlternativesDialog
import com.example.thelastone.ui.StartPreviewDialog
import com.example.thelastone.ui.screens.comp.placedetaildialog.comp.OpeningHoursSection
import com.example.thelastone.ui.screens.comp.placedetaildialog.comp.RatingSection
import com.example.thelastone.ui.state.ErrorState
import com.example.thelastone.ui.state.LoadingState
import com.example.thelastone.utils.buildOpenStatusTextFallback
import com.example.thelastone.utils.openNavigation
import com.example.thelastone.vm.StartFlowViewModel
import com.example.thelastone.vm.StartUiState
import com.example.thelastone.vm.TripDetailUiState
import com.example.thelastone.vm.TripDetailViewModel

@Composable
fun TripDetailScreen(
    padding: PaddingValues,
    viewModel: TripDetailViewModel = hiltViewModel(),
    startVm: StartFlowViewModel = hiltViewModel(),
    // ‼️ 你需要從 NavHost 傳入 NavController 才能啟用聊天室按鈕
    // navController: NavHostController,
    onAddActivity: (tripId: String) -> Unit = {},
    onEditActivity: (tripId: String, activityId: String) -> Unit = { _, _ -> },
    onDeleteActivity: (tripId: String, dayIndex: Int, slotIndex: Int, activityIndex: Int, activity: Activity) -> Unit = { _,_,_,_,_ -> }
) {
    val state by viewModel.state.collectAsState()
    val perms = viewModel.perms.collectAsState().value
    val startState by startVm.ui.collectAsState()
    val context = LocalContext.current

    when (val s = state) {
        is TripDetailUiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
        is TripDetailUiState.Error -> ErrorState(
            modifier = Modifier.padding(padding),
            message = s.message,
            onRetry = viewModel::reload
        )
        is TripDetailUiState.Data -> {
            val trip = s.trip
            var selected by rememberSaveable { mutableIntStateOf(0) }
            var selectedActivityId by remember { mutableStateOf<String?>(null) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { TripInfoCard(trip) } // 👈 標頭

                            // (聊天室按鈕 - 暫時禁用)
                            item {
                                Button(
                                    onClick = {
                                        // ‼️ 需要傳入 NavController 才能啟用
                                        // navController.navigate("chat/${trip.id}")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false
                                ) {
                                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp)) // 👈 修正點
                                    Spacer(Modifier.width(8.dp))
                                    Text("開啟聊天室")
                                }
                            }

                            // (Tabs 和 Activities)
                            dayTabsAndActivities(
                                trip = trip,
                                selected = selected,
                                onSelect = { selected = it },
                                onActivityClick = { dayIdx, slotIdx, actIdx, act ->
                                    selectedActivityId = act.id
                                }
                            )

                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }

                    if (perms?.canEditTrip == true) {
                        FloatingActionButton(
                            onClick = { onAddActivity(trip.id) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        ) { Icon(Icons.Filled.Add, null) }
                    }
                }
            }

            // (findActivityById 邏輯)
            val resolved = selectedActivityId?.let { trip.findActivityById(it) }
            if (resolved == null && selectedActivityId != null) {
                LaunchedEffect(Unit) { selectedActivityId = null }
            }

            // (ActivityBottomSheet 邏輯)
            resolved?.let { location ->
                val (dayIdx, slotIdx, actIdx, act) = location

                ActivityBottomSheet(
                    activity = act,
                    readOnly = perms?.readOnly == true,
                    canEdit  = perms?.canEditTrip == true,
                    onDismiss = { selectedActivityId = null },
                    onEdit = {
                        onEditActivity(trip.id, act.id)
                        selectedActivityId = null
                    },
                    onDelete = {
                        onDeleteActivity(trip.id, dayIdx, slotIdx, actIdx, act)
                        selectedActivityId = null
                    },
                    onGoMaps = { openInMaps(context, act) },
                    // ✅ 修正「出發」按鈕
                    onStart = { startVm.start(act.toLegacyPlace()) }
                )
            }

            // (Start 流程邏輯)
            when (val st = startState) {
                StartUiState.Idle -> Unit
                StartUiState.Loading -> { /* ... (AlertDialog) ... */ }
                is StartUiState.Preview -> {
                    StartPreviewDialog(
                        info = st.info,
                        onDismiss = { startVm.reset() },
                        onConfirmDepart = {
                            val act = resolved?.activity
                            if (act != null) {
                                openNavigation(context, act.lat, act.lng, act.name)
                            }
                            startVm.reset()
                            selectedActivityId = null
                        },
                        onChangePlan = { startVm.showAlternatives() }
                    )
                }
                is StartUiState.Alternatives -> {
                    AlternativesDialog(
                        alts = st.alts,
                        onDismiss = { startVm.reset() },
                        // 🔽🔽 ‼️ 這是你要求的修正點 ‼️ 🔽🔽
                        onPick = { alt -> // 'alt' 是使用者選擇的新景點
                            val latestAct = resolved?.activity // 'latestAct' 是舊景點
                            if (latestAct != null) {
                                // ✅ 呼叫 ViewModel 執行 API 替換
                                viewModel.replaceActivity(latestAct, alt)
                            }
                            startVm.reset()          // 關閉 StartFlow 彈窗
                            selectedActivityId = null // 關閉 ActivityBottomSheet
                        },
                        // 🔼🔼
                        onSeeMore = { startVm.loadMore() }
                    )
                }
                is StartUiState.Error -> { /* ... (AlertDialog) ... */ }
            }
        }
    }
}

/**
 * Activity 底部彈出視窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityBottomSheet(
    activity: Activity,
    readOnly: Boolean,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onGoMaps: () -> Unit,
    onStart: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var menuOpen by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (canEdit) {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("編輯") }, onClick = { menuOpen = false; onEdit() })
                            DropdownMenuItem(text = { Text("刪除") }, onClick = { menuOpen = false; showConfirm = true })
                        }
                    }
                }
            }

            // 地址
            activity.address?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 時間 (stayMinutes)
            Spacer(Modifier.height(8.dp))
            val time = activity.stayMinutes?.let { "預計停留 $it 分鐘" } ?: "未設定時間"
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium
            )

            // 營業時間
            val hasHours = !activity.openingHours.isNullOrEmpty() ||
                    !activity.openStatusText.isNullOrBlank() ||
                    (activity.openNow != null)
            if (hasHours) {
                Spacer(Modifier.height(4.dp))
                val statusText = activity.openStatusText ?: buildOpenStatusTextFallback(
                    activity.openNow, activity.openingHours
                )

                // ✅ 修正「營業時間怪怪的」：只傳入 statusText
                OpeningHoursSection(
                    hours = emptyList(), // 👈 傳入空列表，避免重複顯示
                    statusText = statusText
                )
            }

            // 評分
            activity.rating?.let { r ->
                RatingSection(
                    rating = r,
                    totalReviews = activity.userRatingsTotal ?: 0
                )
            }

            // 備註
            Text(
                text = activity.note ?: "沒有備註", // 👈 讀取 helper property
                style = MaterialTheme.typography.bodyMedium
            )

            // 按鈕
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onGoMaps, modifier = Modifier.weight(1f)) { Text("Go to Maps") }
                OutlinedButton(onClick = onStart, modifier = Modifier.weight(1f)) {
                    Text("Start" )
                }
            }
        }
    }

    // 確認刪除對話框
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("確認刪除") },
            text = { Text("你確定要刪除此活動嗎？此操作無法復原。") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onDelete()
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
}

/**
 * 修正：openInMaps 現在直接從 Activity 讀取 lat/lng/name
 */
private fun openInMaps(context: Context, activity: Activity) {
    val lat = activity.lat
    val lng = activity.lng
    val name = activity.name

    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(name)})")

    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val web = Uri.parse("http://googleusercontent.com/maps/google.com/50{Uri.encode(name)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, web))
    }
}

/**
 * 輔助 data class
 */
data class ActivityLocation(
    val dayIndex: Int,
    val slotIndex: Int,
    val activityIndex: Int,
    val activity: Activity
)

/**
 * 輔助函式：用 ID 查找 Activity
 */
fun Trip.findActivityById(activityId: String): ActivityLocation? {
    days.forEachIndexed { dayIndex, daySchedule ->
        daySchedule.slots.forEachIndexed { slotIndex, slot ->
            slot.places.forEachIndexed { activityIndex, activity ->
                if (activity.id == activityId) {
                    return ActivityLocation(dayIndex, slotIndex, activityIndex, activity)
                }
            }
        }
    }
    return null
}

/**
 * ✅ 新增：'toLegacyPlace' 轉接器函式
 * (將*新* Activity 轉回*舊* Place)
 */
private fun Activity.toLegacyPlace(): Place {
    return Place(
        placeId = this.id,
        name = this.name,
        rating = this.rating,
        userRatingsTotal = this.userRatingsTotal,
        address = this.address,
        openingHours = this.openingHours,
        openNow = this.openNow,
        openStatusText = this.openStatusText,
        lat = this.lat,
        lng = this.lng,
        photoUrl = this.photoUrl,
        miniMapUrl = null
    )
}

// -----------------------------------------------------
// 這兩個函式 (TripInfoCard 和 dayTabsAndActivities)
// 你沒有貼上來，我先假設它們存在於別的檔案或這個檔案的底部
// -----------------------------------------------------

// @Composable
// private fun TripInfoCard(trip: Trip) {
//    /* ... */
// }

// @Composable
// private fun LazyItemScope.dayTabsAndActivities(
//    trip: Trip,
//    selected: Int,
//    onSelect: (Int) -> Unit,
//    onActivityClick: (day: Int, slot: Int, actIdx: Int, act: Activity) -> Unit
// ) {
//    /* ... */
// }