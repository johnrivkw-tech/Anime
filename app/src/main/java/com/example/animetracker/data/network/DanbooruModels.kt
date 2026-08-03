package com.example.animetracker.data.network

data class DanbooruPost(
    val id: Long,
    val rating: String? = null,
    val score: Int = 0,
    val tag_string: String = "",
    val tag_string_character: String = "",
    val tag_string_artist: String = "",
    val tag_string_copyright: String = "",
    val file_url: String? = null,
    val large_file_url: String? = null,
    val preview_file_url: String? = null,
    val source: String? = null,
    val image_width: Int = 0,
    val image_height: Int = 0
) {

    val thumbnailUrl: String?
        get() = preview_file_url ?: large_file_url ?: file_url

    val fullImageUrl: String?
        get() = large_file_url ?: file_url ?: preview_file_url

    val characters: List<String>
        get() = tag_string_character.split(' ').filter { it.isNotBlank() }

    val artists: List<String>
        get() = tag_string_artist.split(' ').filter { it.isNotBlank() }
}
