package com.example.animetracker.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
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

/** Jikan/Cloudflare sometimes reject requests that don't look like they came from a real client. */
private class JikanUserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "AnimeTracker-Android/1.0 (+https://github.com)")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}

/**
 * Jikan enforces a fairly strict public rate limit (roughly 3 requests/sec,
 * 60/min), and — being a free proxy in front of MyAnimeList — will
 * occasionally hand back a transient 5xx or just fail to connect even when
 * the request itself was fine. Without any backoff, a burst of requests --
 * even debounced typing -- can trip one of these and the UI reports it as a
 * generic "can't reach MyAnimeList" failure. This interceptor retries a
 * few times with a growing delay before giving up, which absorbs most of
 * those transient hits (429 rate limit, 500/502/503/504 from the upstream,
 * and low-level IO hiccups alike).
 */
private class JikanRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastIoException: IOException? = null
        var response: Response? = null

        while (attempt < 3) {
            try {
                response?.close()
                response = chain.proceed(request)
                lastIoException = null
                if (response.code !in RETRYABLE_CODES) {
                    return response
                }
            } catch (e: IOException) {
                lastIoException = e
                response = null
            }
            attempt += 1
            if (attempt < 3) {
                Thread.sleep(500L * attempt)
            }
        }

        return response ?: throw (lastIoException ?: IOException("Jikan request failed after retries"))
    }

    private companion object {
        val RETRYABLE_CODES = setOf(429, 500, 502, 503, 504)
    }
}

object JikanApi {
    private const val BASE_URL = "https://api.jikan.moe/v4/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(JikanUserAgentInterceptor())
            .addInterceptor(JikanRetryInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
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
