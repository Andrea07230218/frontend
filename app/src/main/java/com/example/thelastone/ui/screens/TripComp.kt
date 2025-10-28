// 檔案路徑：ui/screens/TripComp.kt
package com.example.thelastone.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // 👈 確保 Import
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // 👈 確保 Import
import androidx.compose.material.icons.filled.Schedule // 👈 確保 Import
import androidx.compose.material.icons.filled.Star // 👈 確保 Import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // 👈 確保 Import
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight // 👈 確保 Import
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thelastone.data.model.Activity
import com.example.thelastone.data.model.AgeBand
import com.example.thelastone.data.model.DaySchedule // 👈 確保 Import
import com.example.thelastone.data.model.Slot // 👈 確保 Import
import com.example.thelastone.data.model.Trip
import com.example.thelastone.data.model.User
import com.example.thelastone.ui.screens.comp.Avatar
import com.example.thelastone.ui.state.EmptyState
import kotlinx.coroutines.launch
import java.time.LocalDate // 👈 確保 Import
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException // 👈 確保 Import
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

/**
 * ✅ 新增：安全的日期格式化函式
 * 處理可為空的日期 (String?)
 */
private fun formatDateRange(start: String?, end: String?): String {
    // 1. 檢查傳入的值是否為 null 或空白
    if (start.isNullOrBlank() || end.isNullOrBlank()) {
        return "未指定日期" // 或者回傳 "" (空字串)
    }

    return try {
        // 2. 嘗試解析
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val s = LocalDate.parse(start, inFmt).format(outFmt)
        val e = LocalDate.parse(end, inFmt).format(outFmt)
        "$s – $e"
    } catch (e: DateTimeParseException) {
        // 3. 如果解析失敗，直接回傳原始文字 (現在是安全的)
        "$start – $end"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripInfoCard(
    trip: Trip,
    modifier: Modifier = Modifier,
    tonalSecondary: Boolean = true
) {
    val container = if (tonalSecondary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onContainer = if (tonalSecondary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor   = onContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                trip.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ✅ 修正：使用安全的 formatDateRange 函式
            Text(
                text = formatDateRange(trip.startDate, trip.endDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (trip.activityStart != null && trip.activityEnd != null) {
                Text(
                    "活動時間：${trip.activityStart} ~ ${trip.activityEnd}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(2.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(2.dp)
            ) {
                trip.totalBudget?.let { CompactTag("NT$$it") }
                if (trip.avgAge != AgeBand.IGNORE) CompactTag(trip.avgAge.label())
                trip.styles.forEach { CompactTag(it) }
                trip.transportPreferences.forEach { CompactTag(it) }
            }
            Spacer(Modifier.height(2.dp))
            if (trip.members.isNotEmpty()) {
                MembersSection(members = trip.members)
            }
        }
    }
}

@Composable
fun CompactTag(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun MembersSection(
    members: List<User>,
    maxShown: Int = 5
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Members（${members.size}）",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            members.take(maxShown).forEach { user ->
                AvatarNameHint(user = user, size = 32.dp)
            }
            val more = members.size - maxShown
            if (more > 0) {
                AssistChip(
                    onClick = {},
                    label = { Text("+$more") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun AvatarNameHint(user: User, size: Dp) {
    var show by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.semantics { contentDescription = user.name }
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    show = true
                    scope.launch {
                        kotlinx.coroutines.delay(1500)
                        show = false
                    }
                }
        ) {
            Avatar(imageUrl = user.avatarUrl, size = size)
        }

        if (show) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = user.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}


private fun AgeBand.label(): String = when (this) {
    AgeBand.IGNORE -> "不列入"
    AgeBand.UNDER_17 -> "17以下"
    AgeBand.A18_25 -> "18–25"
    AgeBand.A26_35 -> "26–35"
    AgeBand.A36_45 -> "36–45"
    AgeBand.A46_55 -> "46–55"
    AgeBand.A56_PLUS -> "56以上"
}

/**
 * ✅ 修正：ActivityRow 現在顯示評分、地址、營業時間
 */
@Composable
private fun ActivityRow(
    activity: Activity,
    slotLabel: String, // 👈 傳入 "上午", "中午" 等
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, // 👈 點擊卡片會觸發 onActivityClick (在 TripDetailScreen 中)
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文字顏色
            val subColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 👈 縮小間距
            ) {
                // 1. 景點名稱 (保持不變)
                Text(activity.name, style = MaterialTheme.typography.titleMedium)

                // 2. 顯示停留時間 (保持不變)
                val time = activity.stayMinutes?.let { "預計停留 $it 分鐘" } ?: slotLabel
                Text(time, style = MaterialTheme.typography.bodyMedium, color = subColor)

                // 🔽🔽 3. ‼️ 加入評分 (來自 Logcat: "rating": 4.4, "reviews": 4234) ‼️ 🔽🔽
                activity.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "評分",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFC107) // 金色
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            // 顯示評分和評論數 (來自 activity.reviews)
                            text = "$rating (${activity.reviews ?: 0} 則評論)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = subColor
                        )
                    }
                }
                // 🔼🔼

                // 🔽🔽 4. ‼️ 加入地址 (來自 Logcat: "address": "...") ‼️ 🔽🔽
                activity.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 🔼🔼

                // 🔽🔽 5. ‼️ 加入營業時間 (來自 Logcat: "open_text": "09:00–17:00") ‼️ 🔽🔽
                activity.openText?.let { openText ->
                    if (openText != "未提供") { // "未提供" 就不用顯示了
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "營業時間",
                                modifier = Modifier.size(16.dp),
                                tint = subColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = openText,
                                style = MaterialTheme.typography.bodySmall,
                                color = subColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // 🔼🔼
            }

            // (相片 URL 邏輯保持不變，但目前 photoUrl 是 null)
            if (!activity.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = activity.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


/**
 * ✅ 核心修正：
 * 1. onActivityClick 參數更新
 * 2. 檢查 day.slots.isEmpty()
 * 3. 遍歷 day.slots 和 slot.places
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun LazyListScope.dayTabsAndActivities(
    trip: Trip,
    selected: Int,
    onSelect: (Int) -> Unit,
    // 🔽🔽 ‼️ 1. 修改 onActivityClick 的參數 ‼️ 🔽🔽
    onActivityClick: (dayIndex: Int, slotIndex: Int, activityIndex: Int, activity: Activity) -> Unit
) {
    val monthDayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // --- TabRow (保持不變) ---
    stickyHeader {
        ScrollableTabRow(
            selectedTabIndex = selected,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            edgePadding = 0.dp,
            indicator = { pos ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(pos[selected]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            trip.days.forEachIndexed { i, d ->
                // 修正：更安全的日期解析
                val monthDayText = try {
                    LocalDate.parse(d.date, isoFormatter).format(monthDayFormatter)
                } catch (e: Exception) {
                    d.date // 回退
                }

                Tab(
                    selected = selected == i,
                    onClick = { onSelect(i) },
                    text = {
                        Text(
                            text = "第 ${i + 1} 天\n$monthDayText",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // --- 內容 (✅ 修正) ---
    val day = trip.days.getOrNull(selected)

    // 🔽🔽 ‼️ 2. 修正：檢查 day.slots 是否為空 ‼️ 🔽🔽
    if (day == null || day.slots.isEmpty()) {
        // 🔼🔼
        item {
            EmptyState(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                title = "沒有行程",
                description = "尚未產生任何每日活動"
            )
        }
    } else {
        // ✅ 3. 成功：遍歷 Slots 和 Places
        day.slots.forEachIndexed { slotIndex, slot ->
            // (可選) 顯示 Slot 標題，例如 "上午 09:00 - 12:00"
            if (slot.label.isNotBlank()) {
                item {
                    Text(
                        text = "${slot.label} (${slot.window.joinToString(" - ")})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            }

            itemsIndexed(slot.places, key = { _, act -> act.id }) { activityIndex, act ->
                ActivityRow(activity = act, slotLabel = slot.label) {
                    // 傳遞新的索引
                    onActivityClick(selected, slotIndex, activityIndex, act)
                }
            }
        }
    }
}