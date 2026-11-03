package com.narkolep.skkimmer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val midashi: String,            // 見出し
    val candidate: String,         // 変換された漢字
    val isFromSystemDict: Boolean, // 辞書ファイルからの変換ならtrue、ユーザー追加ならfalse
    val timestamp: Long = System.currentTimeMillis() // 並び替え用の時間
)
