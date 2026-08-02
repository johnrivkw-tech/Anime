package com.example.animetracker.data.network

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Wraps Jikan (MAL) calls, turning network errors into a [Result] like the
 * other repositories here.
 *
 * Beyond the original character search, this now also backs an emergency
 * fallback for Home/Search/Schedule when [AniListRepository] calls fail —
 * AniList does occasionally disable its own API during stability issues
 * (see that repository's doc comment). Every function here maps into
 * [AniListMedia]/[AniListAiringSchedule] so the ViewModel can drop
 * fallback results straight into the same state flows the UI already
 * reads, with zero UI-layer changes needed beyond a small "showing
 * MyAnimeList results" notice.
 */
class JikanRepository {

    suspend fun searchCharacters(query: String): Result<List<JikanCharacter>> = safeCall {
        JikanApi.service.searchCharacters(query = query).data
    }

    suspend fun searchAnime(query: String, includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.searchAnime(query = query, sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    suspend fun getTrending(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.getTopAnime(filter = "bypopularity", sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    suspend fun getPopularThisSeason(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.getCurrentSeason(sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    suspend fun getTopRated(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.getTopAnime(filter = null, sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    suspend fun getNewReleases(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.getTopAnime(filter = "airing", sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    suspend fun getRecommended(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        JikanApi.service.getTopAnime(filter = "favorite", sfw = sfwFlag(includeMature)).data.map { it.toAniListMedia() }
    }

    /** Best-effort genre browsing: falls back to unfiltered top-anime for any genre with no clean MAL id in [JIKAN_GENRE_IDS] rather than guessing wrong. */
    suspend fun discoverByGenre(genre: String?, includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        val genreId = genre?.let { JIKAN_GENRE_IDS[it] }
        val results = if (genreId != null) {
            JikanApi.service.getAnimeByGenre(genreIds = genreId.toString(), sfw = sfwFlag(includeMature)).data
        } else {
            JikanApi.service.getTopAnime(filter = "bypopularity", sfw = sfwFlag(includeMature)).data
        }
        results.map { it.toAniListMedia() }
    }

    /**
     * Approximates AniList's per-day airing schedule from Jikan's
     * day-of-week broadcast slots. Two accuracy tradeoffs worth knowing:
     * broadcast times are JST and only carry hour:minute (no guarantee
     * every title's converted local time is exactly right if MAL's data
     * is stale), and there's no per-day episode number available at all,
     * so [AniListAiringSchedule.episode] is set to -1 as an explicit
     * "unknown" sentinel — [ScheduleRow] shows "New episode" instead of a
     * number whenever it sees that.
     */
    suspend fun getAiringSchedule(date: LocalDate, includeMature: Boolean = false): Result<List<AniListAiringSchedule>> = safeCall {
        val dayFilter = date.dayOfWeek.name.lowercase()
        val entries = JikanApi.service.getSchedule(dayFilter = dayFilter, sfw = sfwFlag(includeMature)).data
        entries.map { anime ->
            val jstTime = anime.broadcast?.time?.let { runCatching { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) }.getOrNull() }
            val airingAt = if (jstTime != null) {
                date.atTime(jstTime).atZone(ZoneId.of("Asia/Tokyo")).toEpochSecond()
            } else {
                date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            }
            AniListAiringSchedule(airingAt = airingAt, episode = -1, media = anime.toAniListMedia())
        }
    }

    /** Jikan's sfw param is "true = hide adult content" — inverted from AniList's isAdult, and null (not false) is what actually includes it. */
    private fun sfwFlag(includeMature: Boolean): Boolean? = if (includeMature) null else true

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
