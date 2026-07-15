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
        background = Color(0xFF0E0D12),
        surface = Color(0xFF1B1A22),
        surfaceHigh = Color(0xFF242230),
    ),
    Amethyst(
        displayName = "Amethyst",
        primary = Color(0xFF8B5CF6),
        primaryDim = Color(0xFF6D3FD1),
        secondary = Color(0xFFD946EF),
        background = Color(0xFF120E1A),
        surface = Color(0xFF1E1829),
        surfaceHigh = Color(0xFF281F36),
    ),
    Ocean(
        displayName = "Ocean",
        primary = Color(0xFF3AB0FF),
        primaryDim = Color(0xFF2A87CC),
        secondary = Color(0xFF14B8A6),
        background = Color(0xFF0A1218),
        surface = Color(0xFF141F27),
        surfaceHigh = Color(0xFF1C2C36),
    ),
    Sakura(
        displayName = "Sakura",
        primary = Color(0xFFFF6FA8),
        primaryDim = Color(0xFFCC5885),
        secondary = Color(0xFFFF8B5E),
        background = Color(0xFF170E14),
        surface = Color(0xFF241621),
        surfaceHigh = Color(0xFF301D2B),
    ),
    Emerald(
        displayName = "Emerald",
        primary = Color(0xFF22C55E),
        primaryDim = Color(0xFF1B9E4B),
        secondary = Color(0xFFA3E635),
        background = Color(0xFF0B140F),
        surface = Color(0xFF14201A),
        surfaceHigh = Color(0xFF1C2C22),
    ),
    Crimson(
        displayName = "Crimson",
        primary = Color(0xFFE63946),
        primaryDim = Color(0xFFB82C37),
        secondary = Color(0xFFF4A340),
        background = Color(0xFF160C0C),
        surface = Color(0xFF241515),
        surfaceHigh = Color(0xFF301C1C),
    ),
}
