package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.theme.AppThemeOption

/** SharedPreferences-backed store for the selected color theme. */
class ThemePrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun getTheme(): AppThemeOption {
        val saved = prefs.getString(KEY_THEME, null) ?: return AppThemeOption.Blaze
        return AppThemeOption.entries.find { it.name == saved } ?: AppThemeOption.Blaze
    }

    fun setTheme(theme: AppThemeOption) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    companion object {
        private const val KEY_THEME = "selected_theme"
    }
}
