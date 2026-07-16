package com.example.animetracker.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Jikan is a free, keyless REST wrapper around MyAnimeList
 * (https://api.jikan.moe/v4) — used here just for character search, since
 * MAL's character database (and its own "favorites" counts) is what backs
 * the Profile screen's "Favorite Characters" picker, separate from the
 * AniList-backed anime search elsewhere in the app.
 */
interface JikanApiService {

    @GET("characters")
    suspend fun searchCharacters(
        @Query("q") query: String,
        @Query("limit") limit: Int = 15
    ): JikanCharacterSearchResponse
}

/**
 * Jikan enforces a fairly strict public rate limit (roughly 3 requests/sec,
 * 60/min). Without any backoff, a burst of requests -- even debounced typing
 * -- can trip a 429 that the UI then reports as a generic "can't reach
 * MyAnimeList" failure. This interceptor retries a couple of times with a
 * short delay before giving up, which absorbs most of those transient hits.
 */
private class JikanRateLimitInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var attempt = 0
        while (response.code == 429 && attempt < 2) {
            response.close()
            Thread.sleep(500L * (attempt + 1))
            response = chain.proceed(request)
            attempt += 1
        }
        return response
    }
}

object JikanApi {
    private const val BASE_URL = "https://api.jikan.moe/v4/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(JikanRateLimitInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val service: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JikanApiService::class.java)
    }
}
