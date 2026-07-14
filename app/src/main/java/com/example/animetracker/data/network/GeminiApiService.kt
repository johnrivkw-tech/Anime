package com.example.animetracker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @Headers("Content-Type: application/json")
    @POST("models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

/**
 * Single Retrofit instance for Gemini, mirroring [AniListApi]. Unlike
 * AniList, Gemini requires an API key — see [GeminiRepository], which reads
 * it from BuildConfig.GEMINI_API_KEY (populated from the gitignored
 * local.properties file so the key never gets committed to source control).
 */
object GeminiApi {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
