package com.example.animetracker.data

import android.content.Context
import com.example.animetracker.ui.model.AvatarFrame
import com.example.animetracker.ui.model.NameGradient
import com.example.animetracker.ui.navigation.NavBarStyle
import com.example.animetracker.ui.theme.AppThemeOption

/**
 * SharedPreferences-backed store for the Berries Shop: which cosmetics
 * (exclusive themes, nav bar styles) have been unlocked, and which nav bar
 * style is currently active. Berries spent on unlocks are recorded through
 * the existing [GachaPrefs.addSpentBerries] ledger, so the shop and the
 * gacha draw from the same spendable balance rather than each needing its
 * own accounting.
 */
class CosmeticsPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("cosmetics_prefs", Context.MODE_PRIVATE)

    fun getUnlockedThemeNames(): Set<String> =
        prefs.getStringSet(KEY_UNLOCKED_THEMES, emptySet()) ?: emptySet()

    fun unlockTheme(theme: AppThemeOption) {
        val current = getUnlockedThemeNames().toMutableSet()
        current.add(theme.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, current).apply()
    }

    fun getUnlockedNavStyleNames(): Set<String> =
        prefs.getStringSet(KEY_UNLOCKED_NAV_STYLES, emptySet()) ?: emptySet()

    fun unlockNavStyle(style: NavBarStyle) {
        val current = getUnlockedNavStyleNames().toMutableSet()
        current.add(style.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_NAV_STYLES, current).apply()
    }

    fun getSelectedNavStyle(): NavBarStyle {
        val saved = prefs.getString(KEY_SELECTED_NAV_STYLE, null) ?: return NavBarStyle.SOLID
        return NavBarStyle.entries.find { it.name == saved } ?: NavBarStyle.SOLID
    }

    fun setSelectedNavStyle(style: NavBarStyle) {
        prefs.edit().putString(KEY_SELECTED_NAV_STYLE, style.name).apply()
    }

    fun getUnlockedAvatarFrameNames(): Set<String> =
        prefs.getStringSet(KEY_UNLOCKED_AVATAR_FRAMES, emptySet()) ?: emptySet()

    fun unlockAvatarFrame(frame: AvatarFrame) {
        val current = getUnlockedAvatarFrameNames().toMutableSet()
        current.add(frame.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_AVATAR_FRAMES, current).apply()
    }

    fun getSelectedAvatarFrame(): AvatarFrame {
        val saved = prefs.getString(KEY_SELECTED_AVATAR_FRAME, null) ?: return AvatarFrame.Classic
        return AvatarFrame.entries.find { it.name == saved } ?: AvatarFrame.Classic
    }

    fun setSelectedAvatarFrame(frame: AvatarFrame) {
        prefs.edit().putString(KEY_SELECTED_AVATAR_FRAME, frame.name).apply()
    }

    fun getUnlockedNameGradientNames(): Set<String> =
        prefs.getStringSet(KEY_UNLOCKED_NAME_GRADIENTS, emptySet()) ?: emptySet()

    fun unlockNameGradient(gradient: NameGradient) {
        val current = getUnlockedNameGradientNames().toMutableSet()
        current.add(gradient.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_NAME_GRADIENTS, current).apply()
    }

    fun getSelectedNameGradient(): NameGradient {
        val saved = prefs.getString(KEY_SELECTED_NAME_GRADIENT, null) ?: return NameGradient.Classic
        return NameGradient.entries.find { it.name == saved } ?: NameGradient.Classic
    }

    fun setSelectedNameGradient(gradient: NameGradient) {
        prefs.edit().putString(KEY_SELECTED_NAME_GRADIENT, gradient.name).apply()
    }

    companion object {
        private const val KEY_UNLOCKED_THEMES = "cosmetics_unlocked_themes"
        private const val KEY_UNLOCKED_NAV_STYLES = "cosmetics_unlocked_nav_styles"
        private const val KEY_SELECTED_NAV_STYLE = "cosmetics_selected_nav_style"
        private const val KEY_UNLOCKED_AVATAR_FRAMES = "cosmetics_unlocked_avatar_frames"
        private const val KEY_SELECTED_AVATAR_FRAME = "cosmetics_selected_avatar_frame"
        private const val KEY_UNLOCKED_NAME_GRADIENTS = "cosmetics_unlocked_name_gradients"
        private const val KEY_SELECTED_NAME_GRADIENT = "cosmetics_selected_name_gradient"
    }
}
