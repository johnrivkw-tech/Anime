package com.example.animetracker.data.network

import android.net.Uri
import com.example.animetracker.data.AnimeStatus

/** The custom-scheme redirect URI this app registers with AniList. Must match
 *  both the intent-filter in AndroidManifest.xml and the redirect URL
 *  configured on the AniList OAuth client at
 *  https://anilist.co/settings/developer. */
const val ANILIST_REDIRECT_URI = "rei://anilist-auth"

/**
 * Result of a successful OAuth redirect parse: the bearer token AniList
 * handed back plus how long it's valid for.
 */
data class AniListTokenResult(val accessToken: String, val expiresInSeconds: Long)

// Requests the viewer's own name, id, and avatar — just enough to show a
// "Connected as X" card in Settings.
private val VIEWER_QUERY = """
    query {
      Viewer {
        id
        name
        avatar { large medium }
      }
    }
""".trimIndent()

// Pulls every anime on the signed-in user's list, across every list AniList
// buckets them into (Watching, Completed, Planning, etc — flattened back
// out by the caller). Reuses the same MEDIA_FIELDS selection as the public
// browsing queries so the nested `media` deserializes into a full
// AniListMedia the rest of the app already knows how to render.
private val MEDIA_LIST_COLLECTION_QUERY = """
    query(${'$'}userId: Int) {
      MediaListCollection(userId: ${'$'}userId, type: ANIME) {
        lists {
          entries {
            id
            status
            progress
            score(format: POINT_10)
            media {
              $MEDIA_FIELDS
            }
          }
        }
      }
    }
""".trimIndent()

// Creates or updates a single entry on the user's AniList list. AniList
// upserts by mediaId automatically — no need to know the entry's own id.
private val SAVE_MEDIA_LIST_ENTRY_MUTATION = """
    mutation(${'$'}mediaId: Int, ${'$'}status: MediaListStatus, ${'$'}progress: Int, ${'$'}score: Float) {
      SaveMediaListEntry(mediaId: ${'$'}mediaId, status: ${'$'}status, progress: ${'$'}progress, score: ${'$'}score) {
        id
      }
    }
""".trimIndent()

/**
 * Handles the AniList-account half of sync: building the login URL, parsing
 * the OAuth redirect, and the three authenticated GraphQL calls (who am I /
 * pull my list / push a change to my list). [AniListRepository] stays
 * focused on the public, keyless catalog browsing calls; this class is kept
 * separate since every call here needs a per-user bearer token.
 */
class AniListSyncRepository {

    /**
     * The URL to open in a browser to start login. Uses AniList's *implicit
     * grant* flow (`response_type=token`): the user logs in and approves
     * the app on AniList's own site, then AniList redirects straight back
     * to [ANILIST_REDIRECT_URI] with the access token in the URL fragment —
     * no server-side token exchange or client secret needed.
     */
    fun buildAuthorizationUrl(clientId: String): String =
        "https://anilist.co/api/v2/oauth/authorize" +
            "?client_id=$clientId" +
            "&redirect_uri=$ANILIST_REDIRECT_URI" +
            "&response_type=token"

    /**
     * Pulls the access token out of the OAuth redirect. AniList puts it in
     * the URL *fragment* (`#access_token=...&expires_in=...`) rather than
     * the query string, so [Uri.getFragment] is what has it, not
     * [Uri.getQueryParameter].
     */
    fun parseRedirect(uri: Uri): AniListTokenResult? {
        val fragment = uri.fragment ?: return null
        val params = fragment.split("&")
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null else part.substring(0, idx) to part.substring(idx + 1)
            }
            .toMap()
        val token = params["access_token"] ?: return null
        val expiresIn = params["expires_in"]?.toLongOrNull() ?: (365L * 24 * 60 * 60)
        return AniListTokenResult(token, expiresIn)
    }

    suspend fun fetchViewer(accessToken: String): Result<AniListViewer> = safeCall {
        val response = AniListApi.service.getViewer(
            bearer(accessToken),
            AniListRequest(query = VIEWER_QUERY)
        )
        checkErrors(response.errors)
        response.data?.Viewer ?: throw IllegalStateException("Couldn't read AniList profile")
    }

    /** Every anime on the signed-in user's AniList list, flattened out of AniList's per-status lists. */
    suspend fun fetchAnimeList(accessToken: String, userId: Int): Result<List<AniListMediaListEntry>> = safeCall {
        val response = AniListApi.service.getMediaListCollection(
            bearer(accessToken),
            AniListRequest(query = MEDIA_LIST_COLLECTION_QUERY, variables = mapOf("userId" to userId))
        )
        checkErrors(response.errors)
        response.data?.MediaListCollection?.lists.orEmpty().flatMap { it.entries }
    }

    /** Pushes a local status/progress/score change for [mediaId] up to the user's AniList list. */
    suspend fun updateListEntry(
        accessToken: String,
        mediaId: Int,
        status: AnimeStatus,
        progress: Int,
        score: Int
    ): Result<Unit> = safeCall {
        val response = AniListApi.service.saveMediaListEntry(
            bearer(accessToken),
            AniListRequest(
                query = SAVE_MEDIA_LIST_ENTRY_MUTATION,
                variables = mapOf(
                    "mediaId" to mediaId,
                    "status" to status.toAniListStatus(),
                    "progress" to progress,
                    "score" to score.toDouble()
                )
            )
        )
        checkErrors(response.errors)
        Unit
    }

    private fun bearer(accessToken: String) = "Bearer $accessToken"

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

/**
 * Maps AniList's list-status enum down to this app's simpler three-way
 * [AnimeStatus]. AniList distinguishes a few states this app doesn't:
 * `PAUSED`/`REPEATING` fold into [AnimeStatus.WATCHING] (still an active
 * watch, loosely speaking), and `DROPPED` falls back to
 * [AnimeStatus.PLAN_TO_WATCH] since neither "watching" nor "completed" fits
 * — the title still imports so it's not silently dropped from the sync,
 * just parked in the closest available bucket.
 */
fun String?.toAnimeStatus(): AnimeStatus = when (this) {
    "CURRENT", "REPEATING", "PAUSED" -> AnimeStatus.WATCHING
    "COMPLETED" -> AnimeStatus.COMPLETED
    else -> AnimeStatus.PLAN_TO_WATCH
}

/** The reverse of [toAnimeStatus], for pushing local changes back to AniList. */
fun AnimeStatus.toAniListStatus(): String = when (this) {
    AnimeStatus.WATCHING -> "CURRENT"
    AnimeStatus.COMPLETED -> "COMPLETED"
    AnimeStatus.PLAN_TO_WATCH -> "PLANNING"
}
