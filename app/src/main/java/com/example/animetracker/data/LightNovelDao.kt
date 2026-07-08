package com.example.animetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LightNovelDao {

    @Query("SELECT * FROM light_novel_table ORDER BY addedAt DESC")
    fun getAll(): Flow<List<LightNovelEntity>>

    @Insert
    suspend fun insert(novel: LightNovelEntity)

    @Delete
    suspend fun delete(novel: LightNovelEntity)
}
