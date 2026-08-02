package com.example.animetracker.data.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @Headers("Content-Type: application/json")
    @POST("models/gemini-2.5-flash:generateContent")
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

    // Retrofit's default OkHttpClient uses a 10-second read timeout, which
    // is fine for the AniList calls elsewhere in the app but way too short
    // here — Gemini has to actually reason through the user's whole watch
    // history and generate 8 recommendations as structured JSON, which
    // routinely takes longer than that. Without this, "AI Picks" was
    // reliably failing with a timeout on real requests, not just slow ones.
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
