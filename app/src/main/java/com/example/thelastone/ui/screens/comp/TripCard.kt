// 檔案路徑：ui/screens/comp/TripCard.kt
package com.example.thelastone.ui.screens.comp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thelastone.data.model.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    imageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面圖
            if (imageUrl != null && imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    // ✅ 這裡的呼叫現在是安全的，因為 trip.startDate 是 String?
                    text = formatDateRange(trip.startDate, trip.endDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 格式化日期範圍
 * ✅ 修正：函式參數改為可為空 (nullable) 的 String?
 */
// 🔽🔽 1. 將參數型別改為 String? 🔽🔽
private fun formatDateRange(start: String?, end: String?): String {
    // 2. 檢查傳入的值是否為 null 或空白
    if (start.isNullOrBlank() || end.isNullOrBlank()) {
        return "未指定日期" // 或者回傳 "" (空字串)
    }

    return try {
        // 3. 嘗試解析
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val s = LocalDate.parse(start, inFmt).format(outFmt)
        val e = LocalDate.parse(end, inFmt).format(outFmt)
        "$s – $e"
    } catch (_: Exception) {
        // 4. 如果解析失敗，直接回傳原始文字 (現在是安全的)
        "$start – $end"
    }
}