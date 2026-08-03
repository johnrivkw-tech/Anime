package com.example.animetracker.data.network

/**
 * A single Danbooru post. Danbooru's rating field uses one letter:
 * "g" (general), "s" (sensitive), "q" (questionable), "e" (explicit).
 * [DanbooruRepository] only ever queries `rating:g`, so in practice every
 * post surfaced through this app should already have rating == "g" — but
 * [isGeneral] is kept as a belt-and-suspenders client-side check too, in
 * case Danbooru ever returns something unexpected.
 */
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
    val isGeneral: Boolean get() = rating == "e"

    /** Best thumbnail we have for a grid tile. */
    val thumbnailUrl: String?
        get() = preview_file_url ?: large_file_url ?: file_url

    /** Best image for a full-size viewer. */
    val fullImageUrl: String?
        get() = large_file_url ?: file_url ?: preview_file_url

    val characters: List<String>
        get() = tag_string_character.split(' ').filter { it.isNotBlank() }

    val artists: List<String>
        get() = tag_string_artist.split(' ').filter { it.isNotBlank() }
}
