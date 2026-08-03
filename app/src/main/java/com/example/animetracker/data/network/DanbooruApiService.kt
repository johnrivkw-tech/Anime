package com.example.animetracker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Danbooru's public REST API — free, keyless for read-only calls like this.
 *
 * Every call goes through [DanbooruRepository], which is the only place
 * that's allowed to build the `tags` query string. It always appends
 * `rating:s` (Danbooru's "sensitive" rating) and strips any `rating:` tag a
 * caller might try to pass in, so this service interface itself never sees
 * a request that could return anything outside general/sensitive-rated
 * content — no questionable or explicit results.
 */
interface DanbooruApiService {

    @GET("posts.json")
    suspend fun getPosts(
        @Query("tags") tags: String,
        @Query("limit") limit: Int = 30,
        @Query("page") page: Int = 1
    ): List<DanbooruPost>

    /** Tag type-ahead, used to power the search bar's suggestion dropdown. */
    @GET("tags/autocomplete.json")
    suspend fun autocompleteTags(
        @Query("search[name_matches]") query: String,
        @Query("search[order]") order: String = "count",
        @Query("limit") limit: Int = 12
    ): List<DanbooruTagSuggestion>
}

object DanbooruApi {
    private const val BASE_URL = "https://danbooru.donmai.us/"

    val service: DanbooruApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DanbooruApiService::class.java)
    }
}
