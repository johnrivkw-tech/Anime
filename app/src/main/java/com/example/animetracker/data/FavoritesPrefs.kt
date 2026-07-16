package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.model.FavoriteAnimePick
import com.example.animetracker.ui.model.FavoriteCharacterPick
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences-backed store for the user's curated "Favorite Anime"
 * and "Favorite Characters" shelves on the Profile screen (up to 10 each).
 * A tiny Gson-serialized JSON array is enough here — this is a short,
 * infrequently-changed list, not something that needs Room.
 */
class FavoritesPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getFavoriteAnime(): List<FavoriteAnimePick> =
        readList(KEY_FAVORITE_ANIME, animeListType)

    fun setFavoriteAnime(picks: List<FavoriteAnimePick>) {
        prefs.edit().putString(KEY_FAVORITE_ANIME, gson.toJson(picks)).apply()
    }

    fun getFavoriteCharacters(): List<FavoriteCharacterPick> =
        readList(KEY_FAVORITE_CHARACTERS, characterListType)

    fun setFavoriteCharacters(picks: List<FavoriteCharacterPick>) {
        prefs.edit().putString(KEY_FAVORITE_CHARACTERS, gson.toJson(picks)).apply()
    }

    private fun <T> readList(key: String, type: java.lang.reflect.Type): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            gson.fromJson<List<T>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_FAVORITE_ANIME = "favorite_anime_picks"
        private const val KEY_FAVORITE_CHARACTERS = "favorite_character_picks"
        private val animeListType = object : TypeToken<List<FavoriteAnimePick>>() {}.type
        private val characterListType = object : TypeToken<List<FavoriteCharacterPick>>() {}.type
    }
}
