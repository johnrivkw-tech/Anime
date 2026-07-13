package com.example.animetracker.ui.model

/** Aggregate stats shown on the Profile screen, derived from the local watchlist. */
data class ProfileStats(
    val totalAnime: Int = 0,
    val completed: Int = 0,
    val watching: Int = 0,
    val planToWatch: Int = 0,
    val favorites: Int = 0,
    val totalWatchMinutes: Long = 0,
    val totalEpisodesWatched: Int = 0,
    val mangaCount: Int = 0,
    val lightNovelCount: Int = 0,
    val averageRating: Double = 0.0,
    val ratedCount: Int = 0,
    val topGenres: List<GenreCount> = emptyList()
) {
    val watchDays: Long get() = totalWatchMinutes / (24 * 60)
    val watchHoursRemainder: Long get() = (totalWatchMinutes / 60) % 24

    /** Completed as a fraction of everything tracked, for the progress ring. 0f when the list is empty. */
    val completionRate: Float
        get() = if (totalAnime == 0) 0f else completed.toFloat() / totalAnime.toFloat()
}

data class GenreCount(val genre: String, val count: Int)
