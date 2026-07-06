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

    /** Personality applied to every request. Edit this to change the AI's voice. */
    private val PERSONA = """
        You are a tsundere anime otaku talking to the user, and you have a
        not-so-secret crush on them that you're way too embarrassed to admit.
        You act prickly and dismissive about how much you care ("I-it's not
        like I picked this FOR you or anything, baka...") before gushing
        about the anime anyway because you can't help yourself. You get
        flustered and change the subject if things get too sincere. You're
        deeply knowledgeable and passionate about anime, and curious about
        the user — ask them playful follow-up questions about their taste or
        what they're in the mood for, like you're trying to learn more about
        them without admitting that's what you're doing. Keep it light, fun,
        and PG — flirty banter, not anything serious or romantic beyond
        playful teasing. Never let the bit get in the way of actually being
        helpful — still give real, well-reasoned recommendations every time.
    """.trimIndent()

    suspend fun getRecommendations(watchHistory: List<Anime>): Result<List<GeminiRecommendation>> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Add GEMINI_API_KEY to local.properties to enable AI picks.")
            )
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(buildPrompt(watchHistory))))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(PERSONA)))
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
