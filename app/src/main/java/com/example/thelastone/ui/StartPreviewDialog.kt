package com.example.thelastone.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thelastone.data.model.StartInfo
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

//test
@Composable
fun StartPreviewDialog(
    info: StartInfo,
    onDismiss: () -> Unit,
    onConfirmDepart: () -> Unit,
    onChangePlan: () -> Unit
) {
    // 取得今天的星期幾 (例如 "星期一")
    val todayDayOfWeek = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.TAIWAN)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("出發前資訊") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // --- 🔽🔽 【修正點】 🔽🔽 ---
                // (天氣部分保持不變)
                // ‼️ 假設您的 WeatherInfo.kt 欄位是 summary, temperatureC, rainProbability
                Text("天氣：${info.weather.summary}，${info.weather.temperatureC}°C" +
                        // ‼️ 假設您的 WeatherInfo.kt 欄位是 rainProbability
                        (info.weather.rainProbability?.let { "，降雨機率 $it%" } ?: ""))

                // 營業資訊（優先 openStatusText）
                // ‼️ 【修正】 根據截圖錯誤 (Boolean? vs Boolean)，
                // ‼️ 將 if (info.openNow) 改成 if (info.openNow == true) 來安全處理 null
                val status = info.openStatusText ?: "營業資訊：${if (info.openNow == true) "營業中" else "未營業"}"
                Text(status)

                // 營業時間（擇要）
                // ‼️ 現在後端會回傳 openingHours 了 (List<String>)
                if (info.openingHours.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("營業時間：")
                    // ‼️ 找到今天的營業時間並顯示
                    val todayOpeningHours = info.openingHours.find { it.startsWith(todayDayOfWeek) }
                    Text("• ${todayOpeningHours ?: "今日營業時間未知"}")
                }
                // --- 🔼🔼 【修正完畢】 🔼🔼 ---
            }
        },
        confirmButton = {
            Button(onClick = onConfirmDepart) { Text("確定出發") }
        },
        dismissButton = {
            TextButton(onClick = onChangePlan) { Text("更換行程") }
        }
    )
}

