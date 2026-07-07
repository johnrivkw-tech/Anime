package com.example.animetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A manga the user has added to their library. mangaDexId is the primary key so re-adding the same title just overwrites, never duplicates. */
@Entity(tableName = "manga_table")
data class MangaEntity(
    @PrimaryKey
    val mangaDexId: String,
    val title: String,
    val coverUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
)
