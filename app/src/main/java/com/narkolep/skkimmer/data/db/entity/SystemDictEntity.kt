package com.narkolep.skkimmer.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "system_dictionary", indices = [Index(value = ["midashi"])])
data class SystemDictEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val midashi: String,
    val candidates: String
)
