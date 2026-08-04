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

    // --- Default start tab ---
    // Stored as a Destination route string so it can be handed straight to
    // NavHost's startDestination without another lookup/mapping step.

    fun getDefaultStartRoute(): String = prefs.getString(KEY_DEFAULT_START_ROUTE, "home") ?: "home"
    fun setDefaultStartRoute(route: String) {
        prefs.edit().putString(KEY_DEFAULT_START_ROUTE, route).apply()
    }

    // --- Background style ---
    // True = pure black (best for AMOLED / battery). False = "Midnight",
    // a soft near-black with a cool undertone (the existing Void color) —
    // less harsh in low light, more depth behind cards.

    fun getTrueBlackBackground(): Boolean = prefs.getBoolean(KEY_TRUE_BLACK_BG, true)
    fun setTrueBlackBackground(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRUE_BLACK_BG, enabled).apply()
    }

    // --- Home screen layout ---
    // Which rows show up on the Home feed, so people who don't care about
    // e.g. AI Picks or Top Rated can trim the scroll down to what they want.

    fun getShowNewReleases(): Boolean = prefs.getBoolean(KEY_SHOW_NEW_RELEASES, true)
    fun setShowNewReleases(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NEW_RELEASES, enabled).apply()
    }

    fun getShowPopularSeason(): Boolean = prefs.getBoolean(KEY_SHOW_POPULAR_SEASON, true)
    fun setShowPopularSeason(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_POPULAR_SEASON, enabled).apply()
    }

    fun getShowTopRated(): Boolean = prefs.getBoolean(KEY_SHOW_TOP_RATED, true)
    fun setShowTopRated(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_TOP_RATED, enabled).apply()
    }

    fun getShowTrendingNow(): Boolean = prefs.getBoolean(KEY_SHOW_TRENDING_NOW, true)
    fun setShowTrendingNow(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_TRENDING_NOW, enabled).apply()
    }

    fun getShowRecommended(): Boolean = prefs.getBoolean(KEY_SHOW_RECOMMENDED, true)
    fun setShowRecommended(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RECOMMENDED, enabled).apply()
    }

    fun getShowAiPicks(): Boolean = prefs.getBoolean(KEY_SHOW_AI_PICKS, true)
    fun setShowAiPicks(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_AI_PICKS, enabled).apply()
    }

    companion object {
        private const val KEY_EPISODE_REMINDERS = "notify_episode_reminders"
        private const val KEY_NEW_SEASON_ALERTS = "notify_new_season_alerts"
        private const val KEY_AI_PICK_NUDGES = "notify_ai_pick_nudges"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_DATA_SAVER = "data_saver"
        private const val KEY_DEFAULT_START_ROUTE = "default_start_route"
        private const val KEY_TRUE_BLACK_BG = "true_black_background"
        private const val KEY_SHOW_NEW_RELEASES = "home_show_new_releases"
        private const val KEY_SHOW_POPULAR_SEASON = "home_show_popular_season"
        private const val KEY_SHOW_TOP_RATED = "home_show_top_rated"
        private const val KEY_SHOW_TRENDING_NOW = "home_show_trending_now"
        private const val KEY_SHOW_RECOMMENDED = "home_show_recommended"
        private const val KEY_SHOW_AI_PICKS = "home_show_ai_picks"
    }
}
