package com.narkolep.skkimmer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.narkolep.skkimmer.data.db.dao.DictionaryInfoDao
import com.narkolep.skkimmer.data.db.dao.EmojiDao
import com.narkolep.skkimmer.data.db.dao.HistoryDao
import com.narkolep.skkimmer.data.db.dao.SystemDictDao
import com.narkolep.skkimmer.data.db.entity.DictionaryInfoEntity
import com.narkolep.skkimmer.data.db.entity.EmojiEntity
import com.narkolep.skkimmer.data.db.entity.HistoryEntity
import com.narkolep.skkimmer.data.db.entity.SystemDictEntity

@Database(
    entities = [SystemDictEntity::class, HistoryEntity::class, EmojiEntity::class, DictionaryInfoEntity::class],
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
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skk_history_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}