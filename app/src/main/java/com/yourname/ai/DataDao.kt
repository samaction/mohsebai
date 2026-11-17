package com.yourname.ai

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DataDao {
    @Query("SELECT * FROM my_data")
    suspend fun getAllData(): List<DataEntity>

    @Insert
    suspend fun insertData(data: DataEntity)
}