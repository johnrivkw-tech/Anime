package com.example.animetracker.data.network

class DanbooruRepository {

    suspend fun discover(page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        fetch(tags = "order:score", page = page)
    }

    suspend fun search(query: String, page: Int = 1): Result<List<DanbooruPost>> = safeCall {
        if (query.isBlank()) return@safeCall emptyList()
        fetch(tags = query, page = page)
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

        return (cleaned + "rating:g,s,q,e").joinToString(" ")
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
