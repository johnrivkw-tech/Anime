package com.example.animetracker.data.network

import com.example.animetracker.ui.model.GACHA_FORCED_MYTHIC
import java.time.LocalDate

/** AniList's media ID for the One Piece TV anime — powers the gacha roster fetch. */
const val ONE_PIECE_ANILIST_ID = 21

/** AniList's fixed genre vocabulary, used to populate the Discover filter chips. */
val ANILIST_GENRES = listOf(
    "Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Horror",
    "Mahou Shoujo", "Mecha", "Music", "Mystery", "Psychological", "Romance",
    "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller"
)

// GraphQL field selection shared by every query below, aliasing `status` to
// `rawStatus` so AniListMedia can expose its own human-readable `status`.
private const val MEDIA_FIELDS = """
    id
    idMal
    title { romaji english native }
    episodes
    duration
    averageScore
    rawStatus: status
    season
    seasonYear
    description
    coverImage { extraLarge large }
    bannerImage
    genres
    isAdult
    studios(isMain: true) { nodes { id name } }
    trailer { id site }
"""

private val SEARCH_QUERY = """
    query(${'$'}search: String, ${'$'}sort: [MediaSort], ${'$'}season: MediaSeason, ${'$'}seasonYear: Int, ${'$'}perPage: Int, ${'$'}isAdult: Boolean) {
      Page(perPage: ${'$'}perPage) {
        media(
          search: ${'$'}search
          sort: ${'$'}sort
          season: ${'$'}season
          seasonYear: ${'$'}seasonYear
          type: ANIME
          isAdult: ${'$'}isAdult
        ) {
          $MEDIA_FIELDS
        }
      }
    }
""".trimIndent()

// Extra fields fetched only for the Details screen — a "Seasons & Arcs"
// row of related anime (prequels, sequels, side stories, etc.), keyed off
// AniList's relationType(version: 2), which gives the cleaner/newer set of
// relation labels rather than the legacy ones.
private const val RELATIONS_FIELDS = """
    relations {
      edges {
        relationType(version: 2)
        node {
          id
          type
          format
          title { romaji english native }
          coverImage { extraLarge large }
          episodes
          seasonYear
        }
      }
    }
"""

private val DETAILS_QUERY = """
    query(${'$'}id: Int) {
      Media(id: ${'$'}id, type: ANIME) {
        $MEDIA_FIELDS
        $RELATIONS_FIELDS
      }
    }
""".trimIndent()

private val DISCOVER_QUERY = """
    query(${'$'}genre: String, ${'$'}season: MediaSeason, ${'$'}seasonYear: Int, ${'$'}sort: [MediaSort], ${'$'}perPage: Int, ${'$'}page: Int, ${'$'}isAdult: Boolean) {
      Page(page: ${'$'}page, perPage: ${'$'}perPage) {
        media(
          genre: ${'$'}genre
          season: ${'$'}season
          seasonYear: ${'$'}seasonYear
          sort: ${'$'}sort
          type: ANIME
          isAdult: ${'$'}isAdult
        ) {
          $MEDIA_FIELDS
        }
      }
    }
""".trimIndent()

private val CHARACTERS_QUERY = """
    query(${'$'}id: Int) {
      Media(id: ${'$'}id, type: ANIME) {
        characters(sort: [ROLE], perPage: 15) {
          edges {
            role
            node {
              id
              name { full }
              image { large }
            }
          }
        }
      }
    }
""".trimIndent()

// Powers the One Piece gacha's roster: a much wider, favourites-sorted
// character pull than CHARACTERS_QUERY's role-sorted top 15, and includes
// `favourites` so the gacha can rank rarity by how beloved a character is.
private val GACHA_CHARACTERS_QUERY = """
    query(${'$'}id: Int, ${'$'}page: Int, ${'$'}perPage: Int) {
      Media(id: ${'$'}id, type: ANIME) {
        characters(sort: [FAVOURITES_DESC], page: ${'$'}page, perPage: ${'$'}perPage) {
          pageInfo { hasNextPage }
          edges {
            role
            node {
              id
              name { full }
              image { large }
              favourites
            }
          }
        }
      }
    }
""".trimIndent()

// Used by the Profile screen's "Favorite Characters" picker — searches
// AniList's own character database directly instead of going through
// MyAnimeList/Jikan, which was unreliable (aggressive rate-limiting and
// frequent transient failures).
private val CHARACTER_SEARCH_QUERY = """
    query(${'$'}search: String, ${'$'}perPage: Int) {
      Page(perPage: ${'$'}perPage) {
        characters(search: ${'$'}search, sort: [SEARCH_MATCH]) {
          id
          name { full }
          image { large }
        }
      }
    }
""".trimIndent()

// Uses a leaner field set than MEDIA_FIELDS (no trailer/description/banner)
// since the Schedule screen only ever renders a poster thumbnail + title.
private val AIRING_SCHEDULE_QUERY = """
    query(${'$'}airingAtGreater: Int, ${'$'}airingAtLesser: Int, ${'$'}perPage: Int) {
      Page(perPage: ${'$'}perPage) {
        airingSchedules(
          airingAt_greater: ${'$'}airingAtGreater
          airingAt_lesser: ${'$'}airingAtLesser
          sort: TIME
        ) {
          airingAt
          episode
          media {
            id
            idMal
            title { romaji english native }
            episodes
            duration
            averageScore
            rawStatus: status
            season
            seasonYear
            coverImage { extraLarge large }
            genres
          }
        }
      }
    }
""".trimIndent()

/**
 * Wraps calls to the AniList GraphQL API and turns network/GraphQL errors
 * into a [Result], so the ViewModel doesn't need to know about Retrofit,
 * GraphQL, or HTTP exceptions.
 *
 * AniList has no key-less "current season" endpoint the way Jikan's
 * `/seasons/now` did, so [currentSeason] works out today's quarter
 * (Winter = Jan-Mar, Spring = Apr-Jun, Summer = Jul-Sep, Fall = Oct-Dec)
 * client-side and passes it as the season/seasonYear filter.
 *
 * A couple of the "personalized" home sections are simplified stand-ins for
 * true recommendation logic, since real recs need a signed-in AniList
 * account this app doesn't have:
 *  - "Recommended For You" uses AniList's most-favorited list.
 *  - "New Releases" uses titles sorted by most recent start date.
 *
 * AniList needs no API key for any of this, but it does rate-limit by IP
 * (docs list anywhere from 30 to 90 requests/minute depending on current
 * load), so avoid hammering it in a tight loop.
 */
class AniListRepository {

    suspend fun searchAnime(query: String, includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        val response = AniListApi.service.searchMedia(
            AniListRequest(
                query = SEARCH_QUERY,
                variables = mapOf(
                    "search" to query,
                    "perPage" to 10,
                    "isAdult" to if (includeMature) null else false
                )
            )
        )
        checkErrors(response.errors)
        response.data?.Page?.media ?: emptyList()
    }

    suspend fun getTrending(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        fetchList(sort = "TRENDING_DESC", includeMature = includeMature)
    }

    suspend fun getPopularThisSeason(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        val (season, year) = currentSeason()
        fetchList(sort = "POPULARITY_DESC", season = season, seasonYear = year, includeMature = includeMature)
    }

    suspend fun getTopRated(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        fetchList(sort = "SCORE_DESC", includeMature = includeMature)
    }

    suspend fun getNewReleases(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        fetchList(sort = "START_DATE_DESC", includeMature = includeMature)
    }

    suspend fun getRecommended(includeMature: Boolean = false): Result<List<AniListMedia>> = safeCall {
        fetchList(sort = "FAVOURITES_DESC", includeMature = includeMature)
    }

    /** Browse the catalog by genre/season/year for the Discover tab, sorted by popularity. */
    suspend fun discoverAnime(
        genre: String?,
        season: String?,
        seasonYear: Int?,
        page: Int = 1,
        includeMature: Boolean = false
    ): Result<List<AniListMedia>> = safeCall {
        val response = AniListApi.service.searchMedia(
            AniListRequest(
                query = DISCOVER_QUERY,
                variables = mapOf(
                    "genre" to genre,
                    "season" to season,
                    "seasonYear" to seasonYear,
                    "sort" to listOf("POPULARITY_DESC"),
                    "perPage" to 30,
                    "page" to page,
                    "isAdult" to if (includeMature) null else false
                )
            )
        )
        checkErrors(response.errors)
        response.data?.Page?.media ?: emptyList()
    }

    suspend fun getAnimeDetails(aniListId: Int): Result<AniListMedia> = safeCall {
        val response = AniListApi.service.getMedia(
            AniListRequest(query = DETAILS_QUERY, variables = mapOf("id" to aniListId))
        )
        checkErrors(response.errors)
        response.data?.Media ?: throw IllegalStateException("Anime $aniListId not found")
    }

    suspend fun getAnimeCharacters(aniListId: Int): Result<List<AniListCharacterEdge>> = safeCall {
        val response = AniListApi.service.getCharacters(
            AniListRequest(query = CHARACTERS_QUERY, variables = mapOf("id" to aniListId))
        )
        checkErrors(response.errors)
        response.data?.Media?.characters?.edges ?: emptyList()
    }

    /** Free-text character search backing the Profile screen's Favorite Characters picker. */
    suspend fun searchCharacters(query: String): Result<List<AniListCharacterNode>> = safeCall {
        val response = AniListApi.service.searchCharacters(
            AniListRequest(
                query = CHARACTER_SEARCH_QUERY,
                variables = mapOf("search" to query, "perPage" to 15)
            )
        )
        checkErrors(response.errors)
        response.data?.Page?.characters ?: emptyList()
    }

    /**
     * Wide character roster for the One Piece gacha, sorted by favourites
     * so [assignGachaTiers][com.example.animetracker.ui.model.assignGachaTiers]
     * has a real popularity signal to rank rarity against. Pages through up
     * to [maxPages] * [perPage] characters (default up to 100), stopping
     * early once AniList reports no more pages. Also fetches
     * [GACHA_FORCED_MYTHIC][com.example.animetracker.ui.model.GACHA_FORCED_MYTHIC]
     * characters by name and merges them in, in case a lore-critical Mythic
     * (e.g. Imu) doesn't rank high enough in raw favourites to appear in the
     * first few pages on its own.
     */
    suspend fun getOnePieceGachaRoster(
        aniListId: Int = ONE_PIECE_ANILIST_ID,
        maxPages: Int = 4,
        perPage: Int = 25
    ): Result<List<AniListCharacterNode>> = safeCall {
        val all = mutableListOf<AniListCharacterNode>()
        for (page in 1..maxPages) {
            val response = AniListApi.service.getCharacters(
                AniListRequest(
                    query = GACHA_CHARACTERS_QUERY,
                    variables = mapOf("id" to aniListId, "page" to page, "perPage" to perPage)
                )
            )
            checkErrors(response.errors)
            val connection = response.data?.Media?.characters
            all += connection?.edges.orEmpty().map { it.node }
            if (connection?.pageInfo?.hasNextPage != true) break
        }

        // Guarantee lore-critical Mythic pulls exist in the roster even if
        // they didn't surface in the favourites-sorted pages above.
        for (name in GACHA_FORCED_MYTHIC) {
            if (all.none { it.displayName.contains(name, ignoreCase = true) }) {
                val found = AniListApi.service.searchCharacters(
                    AniListRequest(query = CHARACTER_SEARCH_QUERY, variables = mapOf("search" to name, "perPage" to 3))
                ).data?.Page?.characters.orEmpty()
                all += found.filter { it.displayName.contains(name, ignoreCase = true) }
            }
        }

        all.distinctBy { it.id }
    }

    /**
     * Every episode airing between [dayStartEpochSeconds] and
     * [dayEndEpochSeconds] (both Unix seconds, so pass local-midnight
     * boundaries for a given calendar day), oldest first — powers the
     * Schedule tab's per-day list.
     */
    suspend fun getAiringSchedule(
        dayStartEpochSeconds: Long,
        dayEndEpochSeconds: Long
    ): Result<List<AniListAiringSchedule>> = safeCall {
        val response = AniListApi.service.getAiringSchedule(
            AniListRequest(
                query = AIRING_SCHEDULE_QUERY,
                variables = mapOf(
                    "airingAtGreater" to dayStartEpochSeconds,
                    "airingAtLesser" to dayEndEpochSeconds,
                    "perPage" to 50
                )
            )
        )
        checkErrors(response.errors)
        response.data?.Page?.airingSchedules ?: emptyList()
    }

    private suspend fun fetchList(
        sort: String,
        season: String? = null,
        seasonYear: Int? = null,
        includeMature: Boolean = false
    ): List<AniListMedia> {
        val response = AniListApi.service.searchMedia(
            AniListRequest(
                query = SEARCH_QUERY,
                variables = mapOf(
                    "sort" to listOf(sort),
                    "season" to season,
                    "seasonYear" to seasonYear,
                    "perPage" to 10,
                    "isAdult" to if (includeMature) null else false
                )
            )
        )
        checkErrors(response.errors)
        return response.data?.Page?.media ?: emptyList()
    }

    /** The current quarterly anime season, e.g. ("SUMMER", 2026). */
    private fun currentSeason(): Pair<String, Int> {
        val today = LocalDate.now()
        val season = when (today.monthValue) {
            in 1..3 -> "WINTER"
            in 4..6 -> "SPRING"
            in 7..9 -> "SUMMER"
            else -> "FALL"
        }
        return season to today.year
    }

    private fun checkErrors(errors: List<AniListError>?) {
        if (!errors.isNullOrEmpty()) {
            throw IllegalStateException(errors.first().message ?: "AniList API error")
        }
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
