package com.example.animetracker.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/** Body for exchanging an Authorization Code Grant `code` for an access token. */
data class AniListTokenRequest(
    @SerializedName("grant_type") val grantType: String = "authorization_code",
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("redirect_uri") val redirectUri: String,
    @SerializedName("code") val code: String
)

data class AniListTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?
)

/** AniList's OAuth token endpoint lives on `anilist.co`, not `graphql.anilist.co`. */
interface AniListOAuthService {
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/v2/oauth/token")
    suspend fun exchangeToken(@Body request: AniListTokenRequest): AniListTokenResponse
}

object AniListOAuthApi {
    private const val BASE_URL = "https://anilist.co/"

    val service: AniListOAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AniListOAuthService::class.java)
    }
}
