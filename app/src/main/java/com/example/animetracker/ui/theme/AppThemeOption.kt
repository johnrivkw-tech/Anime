package com.example.animetracker.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeOption(
    val displayName: String,
    val primary: Color,
    val primaryDim: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
) {
    Blaze(
        displayName = "Blaze",
        primary = Color(0xFFFF5A1F),
        primaryDim = Color(0xFFCC4818),
        secondary = Color(0xFFFF2D6B),
        background = Color(0xFF000000),
        surface = Color(0xFF121014),
        surfaceHigh = Color(0xFF1C1920),
    ),
    Amethyst(
        displayName = "Amethyst",
        primary = Color(0xFF8B5CF6),
        primaryDim = Color(0xFF6D3FD1),
        secondary = Color(0xFFD946EF),
        background = Color(0xFF000000),
        surface = Color(0xFF131019),
        surfaceHigh = Color(0xFF1D1826),
    ),
    Ocean(
        displayName = "Ocean",
        primary = Color(0xFF3AB0FF),
        primaryDim = Color(0xFF2A87CC),
        secondary = Color(0xFF14B8A6),
        background = Color(0xFF000000),
        surface = Color(0xFF0E1418),
        surfaceHigh = Color(0xFF162026),
    ),
    Sakura(
        displayName = "Sakura",
        primary = Color(0xFFFF6FA8),
        primaryDim = Color(0xFFCC5885),
        secondary = Color(0xFFFF8B5E),
        background = Color(0xFF000000),
        surface = Color(0xFF160F14),
        surfaceHigh = Color(0xFF201720),
    ),
    Emerald(
        displayName = "Emerald",
        primary = Color(0xFF22C55E),
        primaryDim = Color(0xFF1B9E4B),
        secondary = Color(0xFFA3E635),
        background = Color(0xFF000000),
        surface = Color(0xFF0D1410),
        surfaceHigh = Color(0xFF162018),
    ),
    Crimson(
        displayName = "Crimson",
        primary = Color(0xFFE63946),
        primaryDim = Color(0xFFB82C37),
        secondary = Color(0xFFF4A340),
        background = Color(0xFF000000),
        surface = Color(0xFF150E0E),
        surfaceHigh = Color(0xFF201616),
    ),
}
