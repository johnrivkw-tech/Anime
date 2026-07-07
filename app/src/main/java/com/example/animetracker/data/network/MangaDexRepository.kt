package com.example.animetracker.data.network

class MangaDexRepository {

    suspend fun searchManga(query: String): Result<List<MangaDexManga>> = safeCall {
        MangaDexApi.service.searchManga(title = query).data
    }

    suspend fun getChapters(mangaId: String): Result<List<MangaDexChapter>> = safeCall {
        MangaDexApi.service.getChapterFeed(mangaId = mangaId).data
    }

    /** Page image URLs for a chapter, already in reading order. */
    suspend fun getPageUrls(chapterId: String): Result<List<String>> = safeCall {
        val response = MangaDexApi.service.getAtHomeServer(chapterId)
        response.chapter.data.map { fileName ->
            "${response.baseUrl}/data/${response.chapter.hash}/$fileName"
        }
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
