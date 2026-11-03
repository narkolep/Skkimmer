package com.narkolep.skkimmer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.narkolep.skkimmer.data.db.entity.DictionaryInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryInfoDao {
    @Insert
    suspend fun insert(info: DictionaryInfoEntity)

    @Query("SELECT * FROM dictionary_info")
    fun getAllFlow(): Flow<List<DictionaryInfoEntity>>

    @Query("DELETE FROM dictionary_info")
    suspend fun deleteAll()
}