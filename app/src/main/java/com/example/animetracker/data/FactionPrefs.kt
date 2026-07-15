package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.model.Faction

/** SharedPreferences-backed store for the user's chosen rank faction (Pirate/Marine). */
class FactionPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("faction_prefs", Context.MODE_PRIVATE)

    fun getFaction(): Faction {
        val saved = prefs.getString(KEY_FACTION, null) ?: return Faction.PIRATE
        return Faction.entries.find { it.name == saved } ?: Faction.PIRATE
    }

    fun setFaction(faction: Faction) {
        prefs.edit().putString(KEY_FACTION, faction.name).apply()
    }

    companion object {
        private const val KEY_FACTION = "selected_faction"
    }
}
