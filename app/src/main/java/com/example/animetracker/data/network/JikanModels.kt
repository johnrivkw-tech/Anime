package com.example.animetracker.data.network

import com.google.gson.annotations.SerializedName

data class JikanCharacterSearchResponse(
    val data: List<JikanCharacter> = emptyList()
)

data class JikanCharacter(
    @SerializedName("mal_id") val malId: Int,
    val name: String?,
    val images: JikanCharacterImages?,
    val favorites: Int? = null
) {
    val displayName: String get() = name ?: "Unknown"
    val imageUrl: String? get() = images?.jpg?.imageUrl
}

data class JikanCharacterImages(
    val jpg: JikanImageSet?
)

data class JikanImageSet(
    @SerializedName("image_url") val imageUrl: String?
)

// --- Anime fallback (Home/Search/Schedule when AniList is unavailable) ----

data class JikanAnimeListResponse(
    val data: List<JikanAnimeData> = emptyList()
)

data class JikanAnimeData(
    @SerializedName("mal_id") val malId: Int,
    val title: String?,
    @SerializedName("title_english") val titleEnglish: String?,
    val synopsis: String?,
    val episodes: Int?,
    val score: Double?,
    val status: String?,
    val genres: List<JikanGenreTag> = emptyList(),
    val images: JikanAnimeImages?,
    val broadcast: JikanBroadcast? = null
)

data class JikanGenreTag(val name: String?)

data class JikanAnimeImages(val jpg: JikanAnimeImageSet?)

data class JikanAnimeImageSet(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?
)

/** Jikan's broadcast slot is a day-of-week + local JST time, e.g. day="Fridays", time="17:00" — not an exact per-episode timestamp the way AniList's airingSchedules are. */
data class JikanBroadcast(
    val day: String?,
    val time: String?,
    val string: String?
)

/**
 * MAL's numeric genre IDs for the subset of [ANILIST_GENRES] that map
 * cleanly onto one. Genres with no clean MAL equivalent are left out on
 * purpose — [JikanRepository.discoverByGenre] just falls back to
 * unfiltered top-anime for those rather than guessing wrong.
 */
val JIKAN_GENRE_IDS: Map<String, Int> = mapOf(
    "Action" to 1,
    "Adventure" to 2,
    "Comedy" to 4,
    "Drama" to 8,
    "Ecchi" to 9,
    "Fantasy" to 10,
    "Horror" to 14,
    "Mecha" to 18,
    "Music" to 19,
    "Mystery" to 7,
    "Psychological" to 40,
    "Romance" to 22,
    "Sci-Fi" to 24,
    "Slice of Life" to 36,
    "Sports" to 30,
    "Supernatural" to 37,
    "Thriller" to 41
)

/**
 * Maps a Jikan/MAL anime into the app's [AniListMedia] shape so fallback
 * results flow through every existing screen (Home, Search, Schedule)
 * completely unchanged — same poster cards, same grid, same mapper
 * functions. [id] is deliberately set to the *negative* of the MAL id: a
 * real AniList id is never negative, so this doubles as a sentinel other
 * code can check for ("this came from the fallback, don't trust it as a
 * real AniList id") without needing a separate flag threaded through
 * every layer. Tapping into details on a fallback card will 404 against
 * AniList by design — there's no MAL-backed details screen — which is an
 * acceptable rough edge for an emergency-outage fallback whose whole job
 * is just to keep the browsing lists populated.
 */
fun JikanAnimeData.toAniListMedia(): AniListMedia = AniListMedia(
    id = -malId,
    idMal = malId,
    title = AniListTitle(romaji = title, english = titleEnglish, native = null),
    episodes = episodes,
    duration = null,
    averageScore = score?.let { (it * 10).toInt() },
    rawStatus = when (status) {
        "Finished Airing" -> "FINISHED"
        "Currently Airing" -> "RELEASING"
        "Not yet aired" -> "NOT_YET_RELEASED"
        else -> null
    },
    season = null,
    seasonYear = null,
    description = synopsis,
    coverImage = AniListCoverImage(
        extraLarge = images?.jpg?.largeImageUrl,
        large = images?.jpg?.imageUrl
    ),
    bannerImage = null,
    genres = genres.mapNotNull { it.name }
)
