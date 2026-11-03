package com.narkolep.skkimmer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.narkolep.skkimmer.data.db.entity.EmojiEntity

@Dao
interface EmojiDao {
    /* 保存 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emojis: List<EmojiEntity>)

    /* 全ての絵文字を順番通りに取得する */
    @Query("SELECT * FROM emoji_table ORDER BY categoryOrder, subcategoryOrder, emojiOrder")
    suspend fun getAllEmojis(): List<EmojiEntity>

    /* 履歴機能用：最近使った絵文字を取得する */
    @Query("SELECT * FROM emoji_table WHERE lastUsedAt > 0 ORDER BY lastUsedAt DESC LIMIT 30")
    suspend fun getRecentEmojis(): List<EmojiEntity>

    /* 履歴機能用：使った時間を更新する */
    @Query("UPDATE emoji_table SET lastUsedAt = :time WHERE unicode = :unicode")
    suspend fun updateLastUsed(unicode: String, time: Long)
}
