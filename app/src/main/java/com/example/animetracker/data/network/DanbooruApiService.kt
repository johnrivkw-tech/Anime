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
 * `rating:g` (Danbooru's "general" rating) and strips any `rating:` tag a
 * caller might try to pass in, so this service interface itself never sees
 * a request that could return anything outside the general rating.
 */
interface DanbooruApiService {

    @GET("posts.json")
    suspend fun getPosts(
        @Query("tags") tags: String,
        @Query("limit") limit: Int = 30,
        @Query("page") page: Int = 1
    ): List<DanbooruPost>
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
