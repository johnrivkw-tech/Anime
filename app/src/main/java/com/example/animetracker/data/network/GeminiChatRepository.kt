package com.example.animetracker.data.network

import com.example.animetracker.BuildConfig
import com.example.animetracker.data.Anime
import com.google.gson.Gson

/**
 * Handles the back-and-forth "AI Recs" chat. Unlike [GeminiRepository]
 * (one-shot home feed recs), this keeps sending the full conversation so
 * far so Gemini can answer follow-ups like "what about something shorter?"
 * A short system-style turn is prepended each call with the user's local
 * watch history for context, since the Gemini API has no separate system
 * role for generateContent — it's just simulated as the first user/model
 * exchange.
 */
class GeminiChatRepository {

    private val gson = Gson()

    suspend fun sendMessage(
        conversation: List<Pair<Boolean, String>>, // (isUser, text), oldest first, already includes the new message
        watchHistory: List<Anime>
    ): Result<GeminiChatReply> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Add GEMINI_API_KEY to local.properties to enable AI chat.")
            )
        }

        return try {
            val contents = mutableListOf(
                GeminiContent(parts = listOf(GeminiPart(buildContextPrompt(watchHistory))), role = "user"),
                GeminiContent(parts = listOf(GeminiPart("Understood — I'll recommend based on that.")), role = "model")
            )
            conversation.forEach { (isUser, text) ->
                contents.add(GeminiContent(parts = listOf(GeminiPart(text)), role = if (isUser) "user" else "model"))
            }

            val request = GeminiRequest(contents = contents)
            val response = GeminiApi.service.generateContent(BuildConfig.GEMINI_API_KEY, request)

            if (response.error != null) {
                throw IllegalStateException(response.error.message ?: "Gemini API error")
            }

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: throw IllegalStateException("Empty response from Gemini")

            val cleaned = text.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```")
                .trim()

            Result.success(gson.fromJson(cleaned, GeminiChatReply::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildContextPrompt(watchHistory: List<Anime>): String {
        val historyLines = if (watchHistory.isEmpty()) {
            "The user hasn't tracked any anime yet."
        } else {
            watchHistory.joinToString("\n") { anime ->
                val ratingText = if (anime.rating > 0) "${anime.rating}/10" else "unrated"
                "- ${anime.name} (status: ${anime.status.label}, rating: $ratingText)"
            }
        }

        return """
            You are Vizora's AI anime recommendation assistant — a friendly,
            knowledgeable anime expert chatting inside an anime tracking app.
            The user's watch history:
            $historyLines

            Chat naturally and answer their questions. Whenever you recommend
            specific titles in a reply, ALSO list them in a structured
            "recommendations" array so the app can show poster art for them.
            If a reply doesn't include recommendations, use an empty array.
            Use official English or romaji titles so they're easy to look up.

            From now on, respond with ONLY a raw JSON object, no markdown
            formatting, no commentary, in exactly this shape:
            {"reply": "your conversational answer", "recommendations": [{"title": "string", "reason": "one short phrase", "genres": ["string"]}]}
        """.trimIndent()
    }
}
