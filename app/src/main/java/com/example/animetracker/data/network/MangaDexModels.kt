package com.example.animetracker.data.network

data class MangaDexSearchResponse(
    val result: String? = null,
    val data: List<MangaDexManga> = emptyList()
)

data class MangaDexManga(
    val id: String,
    val attributes: MangaDexMangaAttributes,
    val relationships: List<MangaDexRelationship> = emptyList()
) {
    val displayTitle: String
        get() = attributes.title["en"]
            ?: attributes.title.values.firstOrNull()
            ?: attributes.altTitles.firstNotNullOfOrNull { it["en"] }
            ?: "Untitled"

    /** MangaDex serves cover art from a separate uploads host, keyed by manga ID + a filename from the "cover_art" relationship. */
    val coverUrl: String?
        get() = relationships.firstOrNull { it.type == "cover_art" }
            ?.attributes?.fileName
            ?.let { fileName -> "https://uploads.mangadex.org/covers/$id/$fileName.256.jpg" }
}

data class MangaDexMangaAttributes(
    val title: Map<String, String> = emptyMap(),
    val altTitles: List<Map<String, String>> = emptyList(),
    val status: String? = null
)

data class MangaDexRelationship(
    val id: String,
    val type: String,
    val attributes: MangaDexCoverAttributes? = null
)

data class MangaDexCoverAttributes(
    val fileName: String? = null
)

data class MangaDexChapterFeedResponse(
    val result: String? = null,
    val data: List<MangaDexChapter> = emptyList()
)

data class MangaDexChapter(
    val id: String,
    val attributes: MangaDexChapterAttributes
)

data class MangaDexChapterAttributes(
    val chapter: String? = null,
    val title: String? = null,
    val translatedLanguage: String? = null
) {
    val displayLabel: String
        get() {
            val chapterLabel = chapter?.let { "Chapter $it" } ?: "Chapter"
            return if (!title.isNullOrBlank()) "$chapterLabel: $title" else chapterLabel
        }
}

/** MangaDex's page-image handoff: build each page URL as "$baseUrl/data/${chapter.hash}/$fileName". */
data class MangaDexAtHomeResponse(
    val baseUrl: String,
    val chapter: MangaDexAtHomeChapter
)

data class MangaDexAtHomeChapter(
    val hash: String,
    val data: List<String> = emptyList()
)
