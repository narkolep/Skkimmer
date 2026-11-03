package com.narkolep.skkimmer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emoji_table")
data class EmojiEntity(
    @PrimaryKey val unicode: String,
    val idStr: String,
    val emoji: String,
    val description: String,
    val categoryName: String,
    val subcategoryName: String,
    val categoryOrder: Int,    // 元のJSONのカテゴリーの順番を記憶
    val subcategoryOrder: Int, // サブカテゴリーの順番
    val emojiOrder: Int,       // 絵文字の順番
    val lastUsedAt: Long = 0   // 履歴機能用（0なら未使用）
)
