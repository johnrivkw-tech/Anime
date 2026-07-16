package com.example.animetracker.ui.model

/**
 * A single anime the user has hand-picked for their "Favorite Anime" shelf
 * on the Profile screen. Deliberately separate from [Anime.isFavorite]
 * (com.example.animetracker.data.Anime) — that flag just marks entries in
 * the local watchlist, while this is a curated top-10 list that can include
 * any title from AniList, tracked or not.
 */
data class FavoriteAnimePick(
    val aniListId: Int,
    val title: String,
    val imageUrl: String?
)

/** Same idea as [FavoriteAnimePick], but for the "Favorite Characters" shelf. */
data class FavoriteCharacterPick(
    val characterId: Int,
    val name: String,
    val imageUrl: String?
)

/** Hard cap for both shelves, enforced wherever picks are added. */
const val MAX_FAVORITE_PICKS = 10
