package com.example.animetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun AnimeTrackerTheme(
    themeOption: AppThemeOption = AppThemeOption.Blaze,
    // True black is best for AMOLED/battery and is the long-standing
    // default; Midnight swaps in a soft near-black (Void) with a cool
    // undertone for a bit more depth behind cards in low light.
    trueBlackBackground: Boolean = true,
    // Applies across all five main tabs (Home, Schedule, My List, Search,
    // Settings) via MaterialTheme.typography — every Text() that reads its
    // style from the theme picks this up automatically.
    fontOption: AppFontOption = AppFontOption.Default,
    content: @Composable () -> Unit
) {
    val background = if (trueBlackBackground) themeOption.background else Void

    val colorScheme = darkColorScheme(
        primary = themeOption.primary,
        onPrimary = Bone,
        primaryContainer = themeOption.primaryDim,
        onPrimaryContainer = Bone,
        secondary = themeOption.secondary,
        onSecondary = Bone,
        secondaryContainer = themeOption.surface,
        onSecondaryContainer = Bone,
        tertiary = themeOption.secondary,
        onTertiary = Bone,
        background = background,
        onBackground = Bone,
        surface = themeOption.surface,
        onSurface = Bone,
        surfaceVariant = themeOption.surfaceHigh,
        onSurfaceVariant = Smoke,
        outline = DividerColor,
        outlineVariant = DividerColor,
        error = ErrorRed,
        onError = themeOption.background
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(fontOption.fontFamily ?: FontFamily.Default),
        content = content
    )
}
