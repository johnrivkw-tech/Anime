package com.example.animetracker.data

import android.content.Context

/**
 * SharedPreferences-backed store for the content-filter settings: the
 * user's self-reported age, and whether mature (18+) content is allowed
 * to show up across the app. Mirrors the shape of [ThemePrefs].
 */
class ContentFilterPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("content_filter_prefs", Context.MODE_PRIVATE)

    /** Null means the user hasn't entered an age yet. */
    fun getAge(): Int? {
        val saved = prefs.getInt(KEY_AGE, -1)
        return if (saved < 0) null else saved
    }

    fun setAge(age: Int?) {
        prefs.edit().putInt(KEY_AGE, age ?: -1).apply()
    }

    /** Whether the user has explicitly opted in to seeing mature (18+) content. */
    fun getMatureContentEnabled(): Boolean =
        prefs.getBoolean(KEY_MATURE_CONTENT, false)

    fun setMatureContentEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MATURE_CONTENT, enabled).apply()
    }

    companion object {
        private const val KEY_AGE = "user_age"
        private const val KEY_MATURE_CONTENT = "mature_content_enabled"
    }
}
