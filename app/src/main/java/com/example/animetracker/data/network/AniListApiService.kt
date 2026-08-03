package com.example.animetracker.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * AniList exposes a single GraphQL endpoint rather than one REST route per
 * resource, so every call below is a POST of a query string + variables to
 * the same URL; [AniListRepository] decides which query text and variables
 * to send for a given screen.
 */
interface AniListApiService {

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun searchMedia(@Body request: AniListRequest): AniListPageResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun getMedia(@Body request: AniListRequest): AniListMediaResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun getCharacters(@Body request: AniListRequest): AniListCharactersResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun searchCharacters(@Body request: AniListRequest): AniListCharacterSearchResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun getAiringSchedule(@Body request: AniListRequest): AniListScheduleResponse

    // --- Authenticated calls (AniList account sync) ---------------------
    // These three need a per-user "Authorization: Bearer <token>" header,
    // so unlike the calls above (all public/keyless) it's passed explicitly
    // per-request rather than baked into the shared Retrofit client.

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun getViewer(
        @Header("Authorization") authorization: String,
        @Body request: AniListRequest
    ): AniListViewerResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun getMediaListCollection(
        @Header("Authorization") authorization: String,
        @Body request: AniListRequest
    ): AniListMediaListCollectionResponse

    @Headers("Accept: application/json")
    @POST(".")
    suspend fun saveMediaListEntry(
        @Header("Authorization") authorization: String,
        @Body request: AniListRequest
    ): AniListSaveMediaListEntryResponse
}

/**
 * AniList (like Jikan) sits behind Cloudflare, which can reject requests
 * that don't look like they came from a real client — no [okhttp3]
 * default carries any identifying header, so a bare Retrofit setup can get
 * blanket 403'd. This was hitting every AniList call in the app (Home
 * feed, Search, Discover, Schedule) since they all share [AniListApi.service].
 */
private class AniListUserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "AnimeTracker-Android/1.0 (+https://github.com)")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}

/**
 * AniList's public rate limit (docs cite anywhere from 30-90 requests/min
 * depending on current load) means a burst of calls — several home-feed
 * sections loading at once, or fast typing in Search — can trip a
 * transient 429, which without retry logic just surfaces as a hard
 * failure. Mirrors [JikanRetryInterceptor]'s backoff for the same reason.
 */
private class AniListRetryInterceptor : Interceptor {
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

        return response ?: throw (lastIoException ?: IOException("AniList request failed after retries"))
    }

    private companion object {
        val RETRYABLE_CODES = setOf(429, 500, 502, 503, 504)
    }
}

/**
 * Single Retrofit instance for the app. Like Jikan, AniList is public and
 * keyless for read-only queries like these — no auth setup needed here.
 */
object AniListApi {
    private const val BASE_URL = "https://graphql.anilist.co/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AniListUserAgentInterceptor())
            .addInterceptor(AniListRetryInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val service: AniListApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AniListApiService::class.java)
    }
}
