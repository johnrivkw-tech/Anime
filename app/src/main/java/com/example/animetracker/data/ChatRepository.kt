package com.example.animetracker.data

import com.example.animetracker.ui.model.ChatMessage
import com.example.animetracker.ui.model.HomeCardItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the AI recs chat so it survives app restarts, not just
 * configuration changes. The ViewModel only ever sees [ChatMessage] (the UI
 * model); this class handles turning that into a Room row and back,
 * including JSON-encoding the poster recommendations attached to a reply.
 */
class ChatRepository(private val chatDao: ChatDao) {

    private val gson = Gson()
    private val recommendationsType = object : TypeToken<List<HomeCardItem>>() {}.type

    val messages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { it.toChatMessage() }
    }

    suspend fun addMessage(message: ChatMessage) {
        chatDao.insert(message.toEntity())
    }

    suspend fun clearAll() = chatDao.clearAll()

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        val recommendations: List<HomeCardItem> = try {
            gson.fromJson(recommendationsJson, recommendationsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return ChatMessage(isUser = isUser, text = text, recommendations = recommendations)
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
        isUser = isUser,
        text = text,
        recommendationsJson = gson.toJson(recommendations)
    )
}
