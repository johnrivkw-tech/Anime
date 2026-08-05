package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.theme.AppFontOption
import com.example.animetracker.ui.theme.AppThemeOption

/** SharedPreferences-backed store for the selected color theme and app-wide font. */
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

    fun getFont(): AppFontOption {
        val saved = prefs.getString(KEY_FONT, null) ?: return AppFontOption.Default
        return AppFontOption.entries.find { it.name == saved } ?: AppFontOption.Default
    }

    fun setFont(font: AppFontOption) {
        prefs.edit().putString(KEY_FONT, font.name).apply()
    }

    companion object {
        private const val KEY_THEME = "selected_theme"
        private const val KEY_FONT = "selected_font"
    }
}
