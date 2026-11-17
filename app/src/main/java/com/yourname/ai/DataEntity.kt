package com.yourname.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_data")
data class DataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String
)