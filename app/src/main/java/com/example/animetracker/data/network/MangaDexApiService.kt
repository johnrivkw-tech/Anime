package com.example.animetracker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * MangaDex's public REST API — free, keyless for these read-only calls.
 * No content-rating filter is passed anywhere here, so MangaDex's own
 * default applies (safe/suggestive only, erotica and pornographic
 * excluded) rather than us opting into anything more explicit.
 */
interface MangaDexApiService {

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 15,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): MangaDexSearchResponse

    @GET("manga/{id}/feed")
    suspend fun getChapterFeed(
        @Path("id") mangaId: String,
        @Query("translatedLanguage[]") translatedLanguages: List<String> = listOf("en"),
        @Query("order[chapter]") orderChapter: String = "asc",
        @Query("limit") limit: Int = 200
    ): MangaDexChapterFeedResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(@Path("chapterId") chapterId: String): MangaDexAtHomeResponse
}

object MangaDexApi {
    private const val BASE_URL = "https://api.mangadex.org/"

    val service: MangaDexApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaDexApiService::class.java)
    }
}
