package com.example.animetracker.data

import android.content.Context

/**
 * SharedPreferences-backed store for the miscellaneous toggles that live on
 * the Settings screen but don't belong to a more specific prefs class:
 * notification opt-ins and general app-behavior switches. Mirrors the shape
 * of [ThemePrefs] / [ContentFilterPrefs].
 */
class AppSettingsPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    // --- Notifications ---

    fun getEpisodeReminders(): Boolean = prefs.getBoolean(KEY_EPISODE_REMINDERS, true)
    fun setEpisodeReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EPISODE_REMINDERS, enabled).apply()
    }

    fun getNewSeasonAlerts(): Boolean = prefs.getBoolean(KEY_NEW_SEASON_ALERTS, true)
    fun setNewSeasonAlerts(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NEW_SEASON_ALERTS, enabled).apply()
    }

    fun getAiPickNudges(): Boolean = prefs.getBoolean(KEY_AI_PICK_NUDGES, false)
    fun setAiPickNudges(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_PICK_NUDGES, enabled).apply()
    }

    // --- App behavior ---

    fun getReduceMotion(): Boolean = prefs.getBoolean(KEY_REDUCE_MOTION, false)
    fun setReduceMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
    }

    fun getHapticFeedback(): Boolean = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    fun getDataSaver(): Boolean = prefs.getBoolean(KEY_DATA_SAVER, false)
    fun setDataSaver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_SAVER, enabled).apply()
    }

    companion object {
        private const val KEY_EPISODE_REMINDERS = "notify_episode_reminders"
        private const val KEY_NEW_SEASON_ALERTS = "notify_new_season_alerts"
        private const val KEY_AI_PICK_NUDGES = "notify_ai_pick_nudges"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_DATA_SAVER = "data_saver"
    }
}
