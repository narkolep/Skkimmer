package com.narkolep.skkimmer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.narkolep.skkimmer.data.db.entity.SystemDictEntity

@Dao
interface SystemDictDao {
    @Query("SELECT * FROM system_dictionary WHERE midashi = :midashi LIMIT 1")
    suspend fun getEntry(midashi: String): SystemDictEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SystemDictEntity>)

    @Query("DELETE FROM system_dictionary")
    suspend fun deleteAll()
}