package com.narkolep.skkimmer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.*
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dictionary_info")
data class DictionaryInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val entryCount: Int
)

@Dao
interface DictionaryInfoDao {
    @Insert
    suspend fun insert(info: DictionaryInfo)

    @Query("SELECT * FROM dictionary_info")
    fun getAllFlow(): Flow<List<DictionaryInfo>>

    @Query("DELETE FROM dictionary_info")
    suspend fun deleteAll()
}

@Entity(tableName = "system_dictionary", indices = [Index(value = ["midashi"])])
data class SystemDictEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val midashi: String, // 見出し
    val candidates: String // 候補
)

@Dao
interface SystemDictDao {
    @Query("SELECT * FROM system_dictionary WHERE midashi = :midashi LIMIT 1")
    suspend fun getEntry(midashi: String): SystemDictEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SystemDictEntry>)

    @Query("DELETE FROM system_dictionary")
    suspend fun deleteAll()
}

@Entity(tableName = "conversion_history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val midashi: String,            // 見出し
    val candidate: String,         // 変換された漢字
    val isFromSystemDict: Boolean, // 辞書ファイルからの変換ならtrue、ユーザー追加ならfalse
    val timestamp: Long = System.currentTimeMillis() // 並び替え用の時間
)

@Dao
interface HistoryDao {
    @Query("""
    SELECT * FROM conversion_history
    ORDER BY timestamp DESC
    """)
    fun observeAll(): Flow<List<HistoryEntry>>

    @Query("""
    SELECT * FROM conversion_history
    WHERE midashi LIKE '%' || :query || '%'
       OR candidate LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
    """)
    fun searchFlow(query: String): Flow<List<HistoryEntry>>

    // すべて削除
    @Query("DELETE FROM conversion_history")
    suspend fun deleteAll()

    // 見出しに一致する履歴を新しい順に取得
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi ORDER BY timestamp DESC")
    suspend fun getHistoryByMidashi(midashi: String): List<HistoryEntry>

    // 見出しと漢字が完全に一致するものを探す（重複削除用）
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi AND candidate = :candidate")
    suspend fun findEntry(midashi: String, candidate: String): List<HistoryEntry>

    // ID指定で削除
    @Delete
    suspend fun deleteEntry(entry: HistoryEntry)

    // 新規保存
    @Insert
    suspend fun insert(entry: HistoryEntry)

    // 見出しが一致するシステム候補の古いものを取得
    @Query("SELECT * FROM conversion_history WHERE midashi = :midashi AND isFromSystemDict = 1 ORDER BY timestamp ASC")
    suspend fun getOldSystemEntriesByMidashi(midashi: String): List<HistoryEntry>

    // 履歴全体の件数を取得
    @Query("SELECT COUNT(*) FROM conversion_history")
    suspend fun getTotalCount(): Int

    // 履歴全体から最も古いシステム候補を取得（3000件制限用）
    @Query("SELECT * FROM conversion_history WHERE isFromSystemDict = 1 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getOldestSystemEntry(): HistoryEntry?
}

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

@Dao
interface EmojiDao {
    // 保存
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emojis: List<EmojiEntity>)

    // 全ての絵文字を順番通りに取得する
    @Query("SELECT * FROM emoji_table ORDER BY categoryOrder, subcategoryOrder, emojiOrder")
    suspend fun getAllEmojis(): List<EmojiEntity>

    // 履歴機能用：最近使った絵文字を取得する
    @Query("SELECT * FROM emoji_table WHERE lastUsedAt > 0 ORDER BY lastUsedAt DESC LIMIT 30")
    suspend fun getRecentEmojis(): List<EmojiEntity>

    // 履歴機能用：使った時間を更新する
    @Query("UPDATE emoji_table SET lastUsedAt = :time WHERE unicode = :unicode")
    suspend fun updateLastUsed(unicode: String, time: Long)
}

@Database(
    entities = [SystemDictEntry::class, HistoryEntry::class, EmojiEntity::class, DictionaryInfo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun systemDictDao(): SystemDictDao
    abstract fun historyDao(): HistoryDao
    abstract fun emojiDao(): EmojiDao
    abstract fun dictionaryInfoDao(): DictionaryInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skk_history_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}