package com.example.animetracker.data

import android.content.Context

/**
 * SharedPreferences-backed store for the Settings "Show AniList Manga"
 * toggle. Off by default: with it off, manga never appears anywhere
 * (Search stays anime-only, and the Manga list in Settings stays
 * collapsed). Turning it on lets manually-typed searches on the Search
 * screen also check AniList for matching manga titles, and reveals the
 * Manga list (titles the user has tapped into their library) in Settings.
 * Mirrors the shape of [ContentFilterPrefs].
 */
class MangaDisplayPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("manga_display_prefs", Context.MODE_PRIVATE)

    fun getShowAniListManga(): Boolean =
        prefs.getBoolean(KEY_SHOW_ANILIST_MANGA, false)

    fun setShowAniListManga(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ANILIST_MANGA, enabled).apply()
    }

    companion object {
        private const val KEY_SHOW_ANILIST_MANGA = "show_anilist_manga"
    }
}
