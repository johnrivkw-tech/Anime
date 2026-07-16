package com.example.animetracker.data

import android.content.Context

/**
 * SharedPreferences-backed store for the user-customizable AI personality
 * (system prompt) used by both [com.example.animetracker.data.network.GeminiRepository]
 * and [com.example.animetracker.data.network.GeminiChatRepository]. Mirrors
 * the shape of [ThemePrefs] / [ContentFilterPrefs].
 */
class PersonalityPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("personality_prefs", Context.MODE_PRIVATE)

    fun getPersonality(): String =
        prefs.getString(KEY_PERSONALITY, null) ?: DEFAULT_PERSONALITY

    fun setPersonality(personality: String) {
        val trimmed = personality.trim()
        prefs.edit()
            .putString(KEY_PERSONALITY, trimmed.ifBlank { DEFAULT_PERSONALITY })
            .apply()
    }

    fun resetToDefault() {
        prefs.edit().remove(KEY_PERSONALITY).apply()
    }

    companion object {
        private const val KEY_PERSONALITY = "ai_personality"

        /** Used whenever the user hasn't set a custom personality yet. */
        const val DEFAULT_PERSONALITY: String =
            "You are a friendly, knowledgeable anime recommendation assistant. " +
                "You're enthusiastic about anime, manga, and light novels, and you " +
                "give clear, well-reasoned recommendations based on the user's taste. " +
                "Keep your tone warm and conversational."
    }
}
