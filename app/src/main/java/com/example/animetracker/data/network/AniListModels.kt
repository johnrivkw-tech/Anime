package com.example.animetracker.data.network

/**
 * Data models for the AniList GraphQL API (https://anilist.co/graphiql).
 * AniList replaces Jikan (MyAnimeList) here mainly for image quality:
 * [AniListCoverImage.extraLarge] is noticeably sharper than what Jikan
 * served, and every title carries a proper widescreen
 * [AniListMedia.bannerImage] for the Home feed's Featured banner, which
 * Jikan has no equivalent of.
 *
 * A handful of computed properties below (displayTitle, posterUrl, score,
 * status, synopsis, studioNames) exist so the rest of the app can keep
 * reading a flat, friendly shape instead of AniList's raw GraphQL fields.
 */

// --- Request / response envelopes ---------------------------------------

data class AniListRequest(
    val query: String,
    val variables: Map<String, Any?> = emptyMap()
)

data class AniListError(val message: String? = null)

data class AniListPageResponse(
    val data: AniListPageData?,
    val errors: List<AniListError>? = null
)
data class AniListPageData(val Page: AniListPage?)
data class AniListPage(val media: List<AniListMedia> = emptyList())

data class AniListMediaResponse(
    val data: AniListMediaData?,
    val errors: List<AniListError>? = null
)
data class AniListMediaData(val Media: AniListMedia?)

data class AniListCharactersResponse(
    val data: AniListCharactersData?,
    val errors: List<AniListError>? = null
)
data class AniListCharactersData(val Media: AniListCharacterMedia?)
data class AniListCharacterMedia(val characters: AniListCharacterConnection?)
data class AniListCharacterConnection(val edges: List<AniListCharacterEdge> = emptyList())

data class AniListCharacterSearchResponse(
    val data: AniListCharacterSearchData?,
    val errors: List<AniListError>? = null
)
data class AniListCharacterSearchData(val Page: AniListCharacterSearchPage?)
data class AniListCharacterSearchPage(val characters: List<AniListCharacterNode> = emptyList())

data class AniListScheduleResponse(
    val data: AniListScheduleData?,
    val errors: List<AniListError>? = null
)
data class AniListScheduleData(val Page: AniListSchedulePage?)
data class AniListSchedulePage(val airingSchedules: List<AniListAiringSchedule> = emptyList())

/** One episode's airing slot, e.g. "episode 12 of X airs at this Unix time." */
data class AniListAiringSchedule(
    val airingAt: Long,
    val episode: Int,
    val media: AniListMedia
)

// --- Core anime model -----------------------------------------------------

data class AniListMedia(
    val id: Int,
    val idMal: Int?,
    val title: AniListTitle,
    val episodes: Int?,
    val duration: Int?,
    val averageScore: Int?,
    val rawStatus: String?,
    val season: String?,
    val seasonYear: Int?,
    val description: String?,
    val coverImage: AniListCoverImage?,
    val bannerImage: String?,
    val genres: List<String> = emptyList(),
    val isAdult: Boolean = false,
    val studios: AniListStudioConnection? = null,
    val trailer: AniListTrailer? = null,
    val relations: AniListRelationConnection? = null
) {
    /** Prefers the English localized title, falling back through romaji to native script. */
    val displayTitle: String
        get() = title.english ?: title.romaji ?: title.native ?: "Untitled"

    /** Highest-resolution cover art AniList has for this title. */
    val posterUrl: String?
        get() = coverImage?.extraLarge ?: coverImage?.large

    /** Converted from AniList's 0-100 scale to the 0-10 scale the rest of the app displays. */
    val score: Double?
        get() = averageScore?.let { it / 10.0 }

    /** Human-readable status label, in the same style Jikan used to send. */
    val status: String?
        get() = when (rawStatus) {
            "FINISHED" -> "Finished Airing"
            "RELEASING" -> "Currently Airing"
            "NOT_YET_RELEASED" -> "Not Yet Aired"
            "CANCELLED" -> "Cancelled"
            "HIATUS" -> "On Hiatus"
            else -> null
        }

    /** Plain-text synopsis; AniList's raw description often has stray HTML in it. */
    val synopsis: String?
        get() = description?.cleanAniListDescription()

    /** Animation studio(s) only — the query excludes production/licensing companies. */
    val studioNames: List<String>
        get() = studios?.nodes.orEmpty().map { it.name }

    /**
     * Other anime entries (seasons, sequels, side stories, etc.) related to
     * this one, for the Details screen's "Seasons & Arcs" row. Manga/novel
     * source-material relations are dropped since there's nowhere in the
     * app to open them.
     */
    val seasonsAndArcs: List<AniListRelationEdge>
        get() = relations?.edges.orEmpty().filter { it.node.type == "ANIME" }
}

data class AniListTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

data class AniListCoverImage(
    val extraLarge: String?,
    val large: String?
)

data class AniListStudioConnection(
    val nodes: List<AniListStudio> = emptyList()
)

data class AniListStudio(
    val id: Int,
    val name: String
)

data class AniListTrailer(
    val id: String?,
    val site: String?
) {
    /** AniList only ever hosts trailers on YouTube or Dailymotion. */
    val videoUrl: String?
        get() = when (site?.lowercase()) {
            "youtube" -> id?.let { "https://www.youtube.com/watch?v=$it" }
            "dailymotion" -> id?.let { "https://www.dailymotion.com/video/$it" }
            else -> null
        }
}

// --- Relations (seasons, sequels, side stories, etc.) ---------------------

data class AniListRelationConnection(
    val edges: List<AniListRelationEdge> = emptyList()
)

data class AniListRelationEdge(
    val relationType: String?,
    val node: AniListRelationNode
) {
    /** e.g. "PREQUEL" -> "Prequel", "SIDE_STORY" -> "Side story". */
    val relationLabel: String
        get() = relationType
            ?.lowercase()
            ?.replace("_", " ")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Related"
}

data class AniListRelationNode(
    val id: Int,
    val type: String?,
    val format: String?,
    val title: AniListTitle,
    val coverImage: AniListCoverImage?,
    val episodes: Int?,
    val seasonYear: Int?
) {
    val displayTitle: String
        get() = title.english ?: title.romaji ?: title.native ?: "Untitled"

    val posterUrl: String?
        get() = coverImage?.extraLarge ?: coverImage?.large
}

private fun String.cleanAniListDescription(): String = this
    .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("</?(i|b|em|strong)>", RegexOption.IGNORE_CASE), "")
    .replace(Regex("<[^>]*>"), "")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .trim()

// --- Characters -------------------------------------------------------------

data class AniListCharacterEdge(
    val role: String?,
    val node: AniListCharacterNode
) {
    val displayName: String get() = node.name.full ?: "Unknown"
    val imageUrl: String? get() = node.image?.large
}

data class AniListCharacterNode(
    val id: Int,
    val name: AniListCharacterName,
    val image: AniListCharacterImage?
) {
    val displayName: String get() = name.full ?: "Unknown"
    val imageUrl: String? get() = image?.large
}

data class AniListCharacterName(val full: String?)

data class AniListCharacterImage(val large: String?)
