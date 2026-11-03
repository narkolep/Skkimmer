package com.narkolep.skkimmer.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_info")
data class DictionaryInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val entryCount: Int
)
