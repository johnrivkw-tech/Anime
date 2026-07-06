package com.example.animetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted form of a single AI recs chat turn. [recommendationsJson] stores
 * the [com.example.animetracker.ui.model.HomeCardItem] list as raw JSON
 * (via Gson) rather than a Room-relation table, since it's small,
 * write-once, and never queried on its own — see [ChatRepository] for the
 * (de)serialization.
 */
@Entity(tableName = "chat_message_table")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val isUser: Boolean,
    val text: String,
    val recommendationsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
)
