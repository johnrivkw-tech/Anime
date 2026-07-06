package com.example.animetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // Oldest first, so the conversation renders top-to-bottom in order.
    @Query("SELECT * FROM chat_message_table ORDER BY id ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_message_table")
    suspend fun clearAll()
}
