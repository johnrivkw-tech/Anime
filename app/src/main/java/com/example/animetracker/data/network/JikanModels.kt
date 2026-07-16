package com.example.animetracker.data.network

import com.google.gson.annotations.SerializedName

data class JikanCharacterSearchResponse(
    val data: List<JikanCharacter> = emptyList()
)

data class JikanCharacter(
    @SerializedName("mal_id") val malId: Int,
    val name: String?,
    val images: JikanCharacterImages?,
    val favorites: Int? = null
) {
    val displayName: String get() = name ?: "Unknown"
    val imageUrl: String? get() = images?.jpg?.imageUrl
}

data class JikanCharacterImages(
    val jpg: JikanImageSet?
)

data class JikanImageSet(
    @SerializedName("image_url") val imageUrl: String?
)
