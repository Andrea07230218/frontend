// 檔案路徑：data/model/User.kt
package com.example.thelastone.data.model

import kotlinx.serialization.Serializable // 👈 1. 加入 import

@Serializable // 👈 2. 在 data class 上方加上標註
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val friends: List<String> = emptyList() // 好友的 userId 列表
)