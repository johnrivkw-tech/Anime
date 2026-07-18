package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.model.GachaCharacter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences-backed store for the One Piece gacha: how many berries
 * have been spent on pulls (subtracted from [com.example.animetracker.ui.model.ProfileStats.berries]
 * to get a spendable balance), which characters have been pulled and how
 * many times, and a cached copy of the last-fetched AniList roster so the
 * gacha screen has something to show instantly instead of re-fetching on
 * every open.
 */
class GachaPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("gacha_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getSpentBerries(): Long = prefs.getLong(KEY_SPENT_BERRIES, 0L)

    fun addSpentBerries(amount: Long) {
        prefs.edit().putLong(KEY_SPENT_BERRIES, getSpentBerries() + amount).apply()
    }

    /** characterId -> number of times pulled (duplicates included). */
    fun getOwnedCounts(): Map<Int, Int> {
        val json = prefs.getString(KEY_OWNED, null) ?: return emptyMap()
        return try {
            gson.fromJson<Map<Int, Int>>(json, ownedType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setOwnedCounts(counts: Map<Int, Int>) {
        prefs.edit().putString(KEY_OWNED, gson.toJson(counts)).apply()
    }

    fun recordPulls(pulled: List<GachaCharacter>) {
        val current = getOwnedCounts().toMutableMap()
        for (character in pulled) {
            current[character.id] = (current[character.id] ?: 0) + 1
        }
        setOwnedCounts(current)
    }

    fun getCachedRoster(): List<GachaCharacter> {
        val json = prefs.getString(KEY_ROSTER, null) ?: return emptyList()
        return try {
            gson.fromJson<List<GachaCharacter>>(json, rosterType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setCachedRoster(roster: List<GachaCharacter>) {
        prefs.edit()
            .putString(KEY_ROSTER, gson.toJson(roster))
            .putLong(KEY_ROSTER_CACHED_AT, System.currentTimeMillis())
            .apply()
    }

    /** True once the cached roster is older than [maxAgeMillis] (default 7 days) or missing. */
    fun isRosterStale(maxAgeMillis: Long = 7L * 24 * 60 * 60 * 1000): Boolean {
        val cachedAt = prefs.getLong(KEY_ROSTER_CACHED_AT, 0L)
        return cachedAt == 0L || System.currentTimeMillis() - cachedAt > maxAgeMillis
    }

    companion object {
        private const val KEY_SPENT_BERRIES = "gacha_spent_berries"
        private const val KEY_OWNED = "gacha_owned_counts"
        private const val KEY_ROSTER = "gacha_cached_roster"
        private const val KEY_ROSTER_CACHED_AT = "gacha_roster_cached_at"
        private val ownedType = object : TypeToken<Map<Int, Int>>() {}.type
        private val rosterType = object : TypeToken<List<GachaCharacter>>() {}.type
    }
}
