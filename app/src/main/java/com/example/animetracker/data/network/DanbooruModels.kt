package com.example.animetracker.data.network

private val VIDEO_EXTENSIONS = setOf("mp4", "webm", "mov")

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
    val file_ext: String = "",
    val source: String? = null,
    val image_width: Int = 0,
    val image_height: Int = 0
) {

    val thumbnailUrl: String?
        get() = preview_file_url ?: large_file_url ?: file_url

    /** True for animated posts (webm/mp4) — these play back as video, with sound, rather than a static image. */
    val isVideo: Boolean
        get() = file_ext.lowercase() in VIDEO_EXTENSIONS

    /**
     * The URL to actually display for the full post. For video posts the
     * `large_file_url`/`preview_file_url` are just static poster frames, so
     * the original `file_url` (the real video) is used instead.
     */
    val fullImageUrl: String?
        get() = if (isVideo) {
            file_url ?: large_file_url ?: preview_file_url
        } else {
            large_file_url ?: file_url ?: preview_file_url
        }

    val characters: List<String>
        get() = tag_string_character.split(' ').filter { it.isNotBlank() }

    val artists: List<String>
        get() = tag_string_artist.split(' ').filter { it.isNotBlank() }

    /** Every tag on the post (general + character + artist + copyright, etc.), for search/discovery chips. */
    val allTags: List<String>
        get() = tag_string.split(' ').filter { it.isNotBlank() }
}

/**
 * A single suggestion from Danbooru's tag-autocomplete endpoint, used to
 * power the type-ahead dropdown under the gallery search bar.
 */
data class DanbooruTagSuggestion(
    val id: Long = 0,
    val name: String = "",
    val post_count: Int = 0,
    val category: Int = 0
) {
    /** Human-readable form for display, e.g. "hatsune_miku" -> "hatsune miku". */
    val displayName: String
        get() = name.replace('_', ' ')
}
