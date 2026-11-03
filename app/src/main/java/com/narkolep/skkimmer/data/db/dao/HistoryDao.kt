package com.narkolep.skkimmer.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.narkolep.skkimmer.data.db.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("""
    SELECT * FROM conversion_history
    ORDER BY timestamp DESC
    """)
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("""
    SELECT * FROM conversion_history
    WHERE midashi LIKE '%' || :query || '%'
       OR candidate LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
    """)
    fun searchFlow(query: String): Flow<List<HistoryEntity>>

    /* すべて削除 */
    @Query("DELETE FROM conversion_history")
    suspend fun deleteAll()

    /* 見出しに一致する履歴を新しい順に取得 */
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi ORDER BY timestamp DESC")
    suspend fun getHistoryByMidashi(midashi: String): List<HistoryEntity>

    /* 見出しと漢字が完全に一致するものを探す（重複削除用） */
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi AND candidate = :candidate")
    suspend fun findEntry(midashi: String, candidate: String): List<HistoryEntity>

    /* ID指定で削除 */
    @Delete
    suspend fun deleteEntry(entry: HistoryEntity)

    /* 新規保存 */
    @Insert
    suspend fun insert(entry: HistoryEntity)

    /* 見出しが一致するシステム候補の古いものを取得 */
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi AND isFromSystemDict = 1 ORDER BY timestamp ASC")
    suspend fun getOldSystemEntriesByMidashi(midashi: String): List<HistoryEntity>

    /* 履歴全体の件数を取得 */
    @Query("SELECT COUNT(*) FROM conversion_history")
    suspend fun getTotalCount(): Int

    /* 履歴全体から最も古いシステム候補を取得 */
    @Query("SELECT * FROM conversion_history WHERE isFromSystemDict = 1 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getOldestSystemEntry(): HistoryEntity?
}
