package com.example.animetracker.data.network

import com.example.animetracker.BuildConfig
import com.example.animetracker.data.Anime
import com.google.gson.Gson

/**
 * Turns the user's local watchlist into a prompt for Gemini and parses its
 * reply into [GeminiRecommendation]s. AniList has no notion of "recommend
 * based on THIS user's taste" without a signed-in account (see
 * [AniListRepository]'s doc comment) — this is where real personalization
 * comes from instead.
 */
class GeminiRepository {

    private val gson = Gson()

    /**
     * Sends a recommendation request to Gemini.
     *
     * @param personality The system-prompt "voice" to use for this request.
     * This is user-customizable in Settings — see [com.example.animetracker.data.PersonalityPrefs].
     */
    suspend fun getRecommendations(
        watchHistory: List<Anime>,
        personality: String
    ): Result<List<GeminiRecommendation>> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Add GEMINI_API_KEY to local.properties to enable AI picks.")
            )
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(buildPrompt(watchHistory))))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(personality)))
            )
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

            // response_mime_type=application/json should mean this is already
            // raw JSON, but strip stray ```json fences just in case.
            val cleaned = text.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```")
                .trim()

            Result.success(gson.fromJson(cleaned, Array<GeminiRecommendation>::class.java).toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(watchHistory: List<Anime>): String {
        val historyLines = watchHistory.joinToString("\n") { anime ->
            val ratingText = if (anime.rating > 0) "${anime.rating}/10" else "unrated"
            "- ${anime.name} (status: ${anime.status.label}, rating: $ratingText)"
        }

        return """
            You are an anime recommendation engine inside a tracking app.
            Here is a user's watch history:
            $historyLines

            Recommend 8 anime this user has NOT already watched, based on
            patterns in their taste (genres, tone, shows they rated highly).
            Prefer well-known titles that are easy to find. Use official
            English or romaji titles.

            Respond with ONLY a raw JSON array, no markdown formatting, no
            commentary, in exactly this shape:
            [{"title": "string", "reason": "one short sentence", "genres": ["string"]}]
        """.trimIndent()
    }
}
