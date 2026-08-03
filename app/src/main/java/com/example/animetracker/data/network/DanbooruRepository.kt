package com.example.animetracker.data.network

class DanbooruRepository {

    suspend fun discover(page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        fetch(tags = "order:score", page = page)
    }

    suspend fun search(query: String, page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        if (query.isBlank()) return@safeCall emptyList()
        fetch(tags = query, page = page)
    }

    /** Tag-name suggestions for the search bar's type-ahead dropdown. Not post content, so no rating filter needed. */
    suspend fun suggestTags(query: String): Result<List<DanbooruTagSuggestion>> = safeCall {
        if (query.isBlank()) return@safeCall emptyList()
        DanbooruApi.service.autocompleteTags(query = query.trim())
    }

    private suspend fun fetch(tags: String, page: Int): List<DanbooruPost> {
        val response = DanbooruApi.service.getPosts(
            tags = buildQuery(tags),
            page = page
        )

        return response.filter {
            !it.thumbnailUrl.isNullOrBlank()
        }
    }

    private fun buildQuery(rawTags: String): String {
        val cleaned = rawTags
            .split(' ')
            .filter { it.isNotBlank() && !it.startsWith("rating:") }

        return (cleaned + "rating:e").joinToString(" ")
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
