package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.data.network.DanbooruPost
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences-backed store for the gallery's "Saved" list — posts the
 * user has hearted on the Danbooru screen. Same lightweight Gson-JSON
 * pattern as [FavoritesPrefs]; this is a short, infrequently-changed list,
 * not something that needs Room. The whole [DanbooruPost] is stored (not
 * just an id) so the Saved tab can render offline without re-fetching.
 */
class DanbooruFavoritesPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("danbooru_favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getSaved(): List<DanbooruPost> {
        val json = prefs.getString(KEY_SAVED, null) ?: return emptyList()
        return try {
            gson.fromJson<List<DanbooruPost>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setSaved(posts: List<DanbooruPost>) {
        prefs.edit().putString(KEY_SAVED, gson.toJson(posts)).apply()
    }

    companion object {
        private const val KEY_SAVED = "saved_posts"
        private val listType = object : TypeToken<List<DanbooruPost>>() {}.type
    }
}
