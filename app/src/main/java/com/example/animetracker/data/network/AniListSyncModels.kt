package com.example.animetracker.data.network

/**
 * Data models for the *authenticated* half of the AniList GraphQL API — the
 * signed-in user's identity and their personal anime list. Kept separate
 * from [AniListModels] (which covers the public, keyless catalog browsing
 * calls) since these only ever get used once a user has logged in.
 */

// --- Viewer (the logged-in AniList user) -----------------------------------

data class AniListViewerResponse(
    val data: AniListViewerData?,
    val errors: List<AniListError>? = null
)
data class AniListViewerData(val Viewer: AniListViewer?)

data class AniListViewer(
    val id: Int,
    val name: String,
    val avatar: AniListViewerAvatar? = null
)

data class AniListViewerAvatar(
    val large: String? = null,
    val medium: String? = null
) {
    val bestUrl: String? get() = large ?: medium
}

// --- MediaListCollection (the viewer's anime list) --------------------------

data class AniListMediaListCollectionResponse(
    val data: AniListMediaListCollectionData?,
    val errors: List<AniListError>? = null
)
data class AniListMediaListCollectionData(val MediaListCollection: AniListMediaListCollection?)
data class AniListMediaListCollection(val lists: List<AniListMediaListGroup> = emptyList())
data class AniListMediaListGroup(val entries: List<AniListMediaListEntry> = emptyList())

/**
 * One title on the user's AniList list. [status] is AniList's raw enum
 * string (`CURRENT`, `PLANNING`, `COMPLETED`, `PAUSED`, `DROPPED`,
 * `REPEATING`) — see [toAnimeStatus] for how that maps down to this app's
 * three-way [com.example.animetracker.data.AnimeStatus]. [score] comes back
 * on AniList's 0-10 scale (requested with `format: POINT_10`), matching
 * [com.example.animetracker.data.Anime.rating] directly.
 */
data class AniListMediaListEntry(
    val id: Int,
    val status: String?,
    val progress: Int = 0,
    val score: Double = 0.0,
    val media: AniListMedia
)

// --- SaveMediaListEntry (pushing a local change back to AniList) ------------

data class AniListSaveMediaListEntryResponse(
    val data: AniListSaveMediaListEntryData?,
    val errors: List<AniListError>? = null
)
data class AniListSaveMediaListEntryData(val SaveMediaListEntry: AniListSavedListEntryId?)
data class AniListSavedListEntryId(val id: Int)
