package com.example.animetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata for a light novel PDF the user picked via the system file
 * picker (Storage Access Framework). We never copy the file itself — we
 * just remember its content URI (with a persisted read permission grant,
 * taken at import time in the UI layer) and hand it back to whatever PDF
 * app is installed when the user taps it.
 */
@Entity(tableName = "light_novel_table")
data class LightNovelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val uri: String,
    val addedAt: Long = System.currentTimeMillis()
)
