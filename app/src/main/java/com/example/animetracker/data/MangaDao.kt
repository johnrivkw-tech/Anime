package com.example.animetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {

    @Query("SELECT * FROM manga_table ORDER BY addedAt DESC")
    fun getAll(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manga: MangaEntity)

    @Delete
    suspend fun delete(manga: MangaEntity)

    @Query("DELETE FROM manga_table")
    suspend fun deleteAll()
}
