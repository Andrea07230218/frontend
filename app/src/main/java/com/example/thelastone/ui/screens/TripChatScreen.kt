// 檔案路徑：ui/screens/TripChatScreen.kt
package com.example.thelastone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.thelastone.data.model.Message
import com.example.thelastone.data.model.PlaceLite
import com.example.thelastone.data.model.Trip
import com.example.thelastone.ui.state.ErrorState
import com.example.thelastone.ui.state.LoadingState
import com.example.thelastone.utils.isAtBottom
import com.example.thelastone.utils.rememberKeyboardOpen
import com.example.thelastone.vm.ChatUiState
import com.example.thelastone.vm.TripChatViewModel
// 🔽🔽 1. 加入 Time/Format Imports 🔽🔽
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
// 🔼🔼

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripChatScreen(
    padding: PaddingValues,
    viewModel: TripChatViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()

    when (val st = s) {
        is ChatUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
        is ChatUiState.Error   -> ErrorState(modifier = Modifier.fillMaxSize().padding(padding), message = st.message, onRetry = {})
        is ChatUiState.Data -> {
            if (st.showTripSheet) {
                // ✅ 這裡會呼叫*修正後*的 TripSheet
                TripSheet(trip = st.trip, onDismiss = { viewModel.toggleTripSheet(false) })
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)              // ← 關鍵：只吃 Scaffold 的 padding
            ) {
                // 訊息清單
                MessagesList(
                    modifier = Modifier.weight(1f),
                    messages = st.messages,
                    myId = st.myId,                        // ← 這裡
                    onSelectSuggestion = viewModel::onSelectSuggestion
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { viewModel.analyze() },
                        enabled = !st.analyzing,
                        label = { Text(if (st.analyzing) "分析中…" else "分析") },
                        leadingIcon = { Icon(Icons.Default.TipsAndUpdates, null) }
                    )
                    AssistChip(
                        onClick = { viewModel.toggleTripSheet(true) },
                        label = { Text("行程") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                    )
                }

                // 輸入列：加上 imePadding 讓鍵盤頂起來時不被遮住
                ChatInputBar(
                    value = st.input,
                    onValueChange = viewModel::updateInput,
                    onSend = viewModel::send,
                    modifier = Modifier
                        .imePadding()               // ← 鍵盤彈出時自動避讓
                        .navigationBarsPadding()    // ← 全螢幕下靠近底部也能避開
                )
            }
        }
    }
}

// --- MessagesList (保持不變) ---
@Composable
private fun MessagesList(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    myId: String,                                   // ← 新增
    onSelectSuggestion: (PlaceLite) -> Unit
) {
    val listState = rememberLazyListState()
    val keyboardOpen by rememberKeyboardOpen()

    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }
    LaunchedEffect(messages.size, keyboardOpen) {
        if (messages.isNotEmpty() && listState.isAtBottom()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            val isAi = msg.isAi
            val isMine = !isAi && msg.sender.id == myId

            val bubbleColor =
                when {
                    isAi   -> MaterialTheme.colorScheme.surfaceVariant
                    isMine -> MaterialTheme.colorScheme.primaryContainer
                    else   -> MaterialTheme.colorScheme.surface
                }

            when {
                // --- Trip AI：整個框置中（內文不變，不做置中） ---
                isAi -> {
                    Box(Modifier.fillMaxWidth()) {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.medium,
                            color = bubbleColor,
                            modifier = Modifier
                                .align(Alignment.Center)      // 置中
                                .widthIn(max = 560.dp)        // 避免太寬；可依你版型調
                                .padding(horizontal = 0.dp)   // 外邊距看需求
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = "Trip AI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(msg.text)

                                val sug = msg.suggestions
                                if (!sug.isNullOrEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        sug.forEach { p ->
                                            SuggestionCard(place = p, onClick = { onSelectSuggestion(p) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 我自己：整塊靠右；"You" 在泡泡外、右上 ---
                isMine -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            // 泡泡外右上角的標籤
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 2.dp, bottom = 4.dp)
                            )

                            // 泡泡本體
                            Surface(
                                tonalElevation = 0.dp,
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .wrapContentWidth()           // ← 寬度依內容
                                    .widthIn(max = 320.dp)        // ← 最多 320dp，避免太長
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        msg.text,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
                // --- 其他人：維持靠左；名稱仍在泡泡內（不變） ---
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            tonalElevation = 0.dp,
                            shape = MaterialTheme.shapes.medium,
                            color = bubbleColor,
                            modifier = Modifier
                                .wrapContentWidth()
                                .widthIn(max = 320.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.sender.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(msg.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SuggestionCard (保持不變) ---
@Composable
private fun SuggestionCard(
    place: PlaceLite,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                place.address?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            place.rating?.let {
                Text("★ $it", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// --- ChatInputBar (保持不變) ---
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("輸入訊息…") },
            singleLine = true
        )
        Button(onClick = onSend) { Text("送出") }
    }
}

/**
 * ✅ 修正：TripSheet 現在會讀取新的 Trip 結構
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripSheet(
    trip: Trip?,
    onDismiss: () -> Unit
) {
    if (trip == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(), // 👈 確保底部有 padding
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(trip.name, style = MaterialTheme.typography.titleLarge)

            // 🔽🔽 2. 修正：使用 formatDateRange 處理可為空的日期 🔽🔽
            Text(
                formatDateRange(trip.startDate, trip.endDate), // 👈 使用安全函式
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 🔼🔼

            Spacer(Modifier.height(8.dp))

            // 🔽🔽 3. 修正：遍歷 day.slots 和 slot.places 🔽🔽
            trip.days.forEachIndexed { idx, day ->
                Text("Day ${idx + 1} - ${day.date}", style = MaterialTheme.typography.titleSmall)
                Column(Modifier.fillMaxWidth().padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    // 遍歷 Slots
                    day.slots.forEach { slot ->
                        // 顯示 Slot 標題 (例如 "上午 09:00 - 12:00")
                        Text(
                            text = "${slot.label} (${slot.window.joinToString(" - ")})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        // 遍歷 Places (Activities)
                        slot.places.forEach { act ->
                            // ✅ 修正：使用 act.name (不再有 act.place)
                            Text(
                                text = "• ${act.name}", // 👈 顯示地點名稱
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            // 🔼🔼
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * 🔽🔽 4. 新增：安全的日期格式化函式 (從 TripComp.kt 複製) 🔽🔽
 */
private fun formatDateRange(start: String?, end: String?): String {
    if (start.isNullOrBlank() || end.isNullOrBlank()) {
        return "未指定日期"
    }
    return try {
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val s = LocalDate.parse(start, inFmt).format(outFmt)
        val e = LocalDate.parse(end, inFmt).format(outFmt)
        "$s – $e"
    } catch (e: DateTimeParseException) {
        "$start – $end"
    }
}