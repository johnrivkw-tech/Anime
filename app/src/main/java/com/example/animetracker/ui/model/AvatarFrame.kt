package com.example.animetracker.ui.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A decorative ring drawn around the profile avatar (Settings header card
 * and the Profile screen). [ringColors] feeds a linear gradient border;
 * an empty list means "use the live theme's primary/secondary" instead of
 * fixed colors, which is how [Classic] stays in sync with whatever accent
 * is currently selected rather than looking stale next to it.
 */
enum class AvatarFrame(
    val displayName: String,
    val ringColors: List<Color>,
    val glow: Boolean = false,
    val berriesCost: Long = 0,
) {
    Classic(
        displayName = "Classic",
        ringColors = emptyList(),
        berriesCost = 0L,
    ),
    StrawHatGold(
        displayName = "Straw Hat Gold",
        ringColors = listOf(Color(0xFFFFC947), Color(0xFFE8A317)),
        berriesCost = 1000L,
    ),
    MarineHalo(
        displayName = "Marine Halo",
        ringColors = listOf(Color(0xFF2B6CB0), Color(0xFFE2E8F0)),
        berriesCost = 1400L,
    ),
    VoidRing(
        displayName = "Void Ring",
        ringColors = listOf(Color(0xFF7C3AED), Color(0xFF0C0A14)),
        glow = true,
        berriesCost = 1800L,
    ),
}

/** Resolves this frame's ring gradient, falling back to the live theme's accent pair for [AvatarFrame.Classic]. */
@Composable
fun AvatarFrame.brush(): Brush {
    return if (ringColors.isEmpty()) {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
    } else {
        Brush.linearGradient(ringColors)
    }
}

