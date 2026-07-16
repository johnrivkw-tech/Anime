package com.example.animetracker.data.network

/** Wraps Jikan (MAL) calls, turning network errors into a [Result] like the other repositories here. */
class JikanRepository {

    suspend fun searchCharacters(query: String): Result<List<JikanCharacter>> = safeCall {
        JikanApi.service.searchCharacters(query = query).data
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
