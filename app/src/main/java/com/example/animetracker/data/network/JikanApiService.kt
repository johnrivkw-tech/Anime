package com.example.animetracker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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

object JikanApi {
    private const val BASE_URL = "https://api.jikan.moe/v4/"

    val service: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JikanApiService::class.java)
    }
}
