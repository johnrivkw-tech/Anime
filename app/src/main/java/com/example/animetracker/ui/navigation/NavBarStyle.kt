package com.example.animetracker.ui.navigation

/**
 * Visual treatment for the bottom nav pill bar. Solid is the long-standing
 * free default; Gradient and Glass are Berries Shop exclusives unlocked
 * with in-app currency, same pattern as [com.example.animetracker.ui.theme.AppThemeOption].
 */
enum class NavBarStyle(
    val displayName: String,
    val description: String,
    val berriesCost: Long = 0,
) {
    SOLID(
        displayName = "Solid",
        description = "The classic flat surface look.",
        berriesCost = 0L,
    ),
    GRADIENT(
        displayName = "Gradient Glow",
        description = "A soft accent gradient sweeps across the bar.",
        berriesCost = 800L,
    ),
    GLASS(
        displayName = "Glass",
        description = "A frosted, translucent bar with a glowing edge.",
        berriesCost = 1200L,
    ),
}
