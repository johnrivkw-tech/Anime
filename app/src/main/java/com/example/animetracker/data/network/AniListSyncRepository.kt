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
     * The URL to open in a browser to start login. Uses the *Authorization
     * Code Grant* (`response_type=code`): the user logs in and approves the
     * app on AniList's own site, then AniList redirects back to
     * [ANILIST_REDIRECT_URI] with a one-time `code` in the query string,
     * which [exchangeCodeForToken] trades for an access token.
     *
     * AniList's docs also describe an *implicit grant* (`response_type=token`,
     * token comes straight back in the redirect, no exchange needed) — that
     * would be simpler for a client-only app like this one, but in practice
     * AniList's server currently rejects it before the login screen even
     * loads, so the Authorization Code Grant is what actually works.
     */
    fun buildAuthorizationUrl(clientId: String): String =
        "https://anilist.co/api/v2/oauth/authorize" +
            "?client_id=$clientId" +
            "&redirect_uri=$ANILIST_REDIRECT_URI" +
            "&response_type=code"

    /** Pulls the one-time authorization `code` out of the OAuth redirect's query string. */
    fun parseRedirectCode(uri: Uri): String? = uri.getQueryParameter("code")

    /**
     * Trades the one-time `code` from [parseRedirectCode] for a real access
     * token by POSTing to AniList's token endpoint.
     */
    suspend fun exchangeCodeForToken(
        clientId: String,
        clientSecret: String,
        code: String
    ): Result<AniListTokenResult> = safeCall {
        try {
            val response = AniListOAuthApi.service.exchangeToken(
                AniListTokenRequest(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    redirectUri = ANILIST_REDIRECT_URI,
                    code = code
                )
            )
            val token = response.accessToken ?: throw IllegalStateException("AniList didn't return an access token")
            AniListTokenResult(token, response.expiresIn ?: (365L * 24 * 60 * 60))
        } catch (e: retrofit2.HttpException) {
            // Surface AniList's actual JSON error (e.g. "invalid_client") rather
            // than just "HTTP 400" — this is what actually shows up in the app
            // so it's diagnosable instead of a generic failure.
            val body = e.response()?.errorBody()?.string()
            throw IllegalStateException("AniList said: ${body ?: e.message()}")
        }
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
