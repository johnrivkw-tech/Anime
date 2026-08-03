package com.example.animetracker.data.network

/**
 * Gallery access is locked to Danbooru's "general" rating (`rating:g`) —
 * always safe-for-work anime/manga art, no exceptions. This is enforced in
 * two layers:
 *
 * 1. [buildQuery] always appends `rating:g` to whatever tags a caller asks
 *    for, and strips out any `rating:` / `-rating:` token the caller tried
 *    to include, so a search query can never override or loosen it.
 * 2. [DanbooruPost.isGeneral] is checked again on the response before a
 *    post is handed back, as a second, independent filter.
 *
 * There is intentionally no way to call the Danbooru API from this app
 * with any other rating.
 */
class DanbooruRepository {

    /** A general front page — trending general-rated art, newest first. */
    suspend fun discover(page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        fetch(tags = "order:score", page = page)
    }

    /** Free-text tag search, e.g. "hatsune_miku" or "genshin_impact 1girl". */
    suspend fun search(query: String, page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        if (query.isBlank()) return@safeCall emptyList()
        fetch(tags = query, page = page)
    }

    private suspend fun fetch(tags: String, page: Int): List<DanbooruPost> {
        val response = DanbooruApi.service.getPosts(tags = buildQuery(tags), page = page)
        return response.filter { it.isGeneral && !it.thumbnailUrl.isNullOrBlank() }
    }

    /** Strips any rating override from [rawTags] and pins the query to `rating:g`. */
    private fun buildQuery(rawTags: String): String {
        val sanitized = rawTags
            .split(' ')
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("rating:", ignoreCase = true) || it.startsWith("-rating:", ignoreCase = true) }
            // Danbooru caps tag-search combinations; leave headroom for the
            // rating tag we always append.
            .take(1)
        return (sanitized + "rating:g").joinToString(" ")
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
