package com.example.animetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AnimeTrackerTheme(
    themeOption: AppThemeOption = AppThemeOption.Blaze,
    content: @Composable () -> Unit
) {
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
        background = themeOption.background,
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
        typography = Typography,
        content = content
    )
}
