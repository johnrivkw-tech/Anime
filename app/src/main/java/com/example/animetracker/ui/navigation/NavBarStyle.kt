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
    FLOATING_DOTS(
        displayName = "Floating Icons",
        description = "Each icon floats in its own separate capsule — no connecting bar.",
        berriesCost = 1600L,
    ),
    DOCK(
        displayName = "Full Dock",
        description = "A square-edged bar flush against the bottom edge, full width.",
        berriesCost = 1400L,
    ),
    UNDERLINE(
        displayName = "Minimal Underline",
        description = "No bar at all — just icons with a sliding underline.",
        berriesCost = 1800L,
    ),
}
