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
    OUTLINE(
        displayName = "Outline",
        description = "A bordered pill with no fill at all — just a hairline and your icons.",
        berriesCost = 600L,
    ),
    SEGMENTED(
        displayName = "Segmented",
        description = "One solid bar split into labeled segments by thin dividers.",
        berriesCost = 1100L,
    ),
    NOTCH(
        displayName = "Notch",
        description = "The selected icon pops up out of the bar in its own raised bubble.",
        berriesCost = 1500L,
    ),
    BUBBLE_POP(
        displayName = "Bubble Pop",
        description = "Each icon floats in its own circle; the selected one lifts and grows.",
        berriesCost = 1900L,
    ),
    ISLANDS(
        displayName = "Islands",
        description = "The bar splits into two separate floating capsules with a gap in the middle.",
        berriesCost = 2200L,
    ),
    AURORA_DRIFT(
        displayName = "Aurora Drift",
        description = "Mythic. A slow-drifting color wash sweeps through the bar under a breathing glow.",
        berriesCost = 6000L,
    ),
    VOID_RIFT(
        displayName = "Void Rift",
        description = "Mythic. A near-black glass bar with a color-cycling rim and a pulsing halo around the selected icon.",
        berriesCost = 7500L,
    ),
}
