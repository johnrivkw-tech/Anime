package com.example.animetracker.ui.model

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle

/**
 * A gradient treatment for the user's display name, shown wherever their
 * name renders: the Settings profile header card and the Profile page
 * name. Tiers get progressively louder; [LegendaryBlaze] is the one
 * splurge option — a moving gold shine sweep plus a flickering warm
 * shadow that reads as a fire glow around the letters.
 */
enum class NameGradient(
    val displayName: String,
    val colors: List<Color>,
    val shine: Boolean = false,
    val fireGlow: Boolean = false,
    val glowColor: Color = Color(0xFFFF6A00),
    /** Speed multiplier for the shine sweep + flicker on [shine] tiers.
     *  1f matches the original Legendary Blaze pace; higher runs faster. */
    val speed: Float = 1f,
    /** Multiplier on the flickering glow's blur radius. 1f matches the
     *  original Legendary Blaze halo size; higher makes the glow bigger
     *  and softer, not just brighter. */
    val glowIntensity: Float = 1f,
    val berriesCost: Long = 0,
) {
    Classic(
        displayName = "Classic",
        colors = emptyList(),
        berriesCost = 0L,
    ),
    OceanBreeze(
        displayName = "Ocean Breeze",
        colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1)),
        berriesCost = 600L,
    ),
    Toxic(
        displayName = "Toxic",
        colors = listOf(Color(0xFFA3E635), Color(0xFF15803D)),
        berriesCost = 900L,
    ),
    RoyalPurple(
        displayName = "Royal Purple",
        colors = listOf(Color(0xFFC084FC), Color(0xFFEC4899)),
        berriesCost = 1300L,
    ),
    BloodMoon(
        displayName = "Blood Moon",
        colors = listOf(Color(0xFFFCA5A5), Color(0xFFDC2626)),
        berriesCost = 1800L,
    ),
    LegendaryBlaze(
        displayName = "Legendary Blaze",
        colors = listOf(
            Color(0xFFFFF3B0),
            Color(0xFFFFC947),
            Color(0xFFFF7A00),
            Color(0xFFFFC947),
            Color(0xFFFFF3B0)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFFFF6A00),
        berriesCost = 5000L,
    ),
    VoidEclipse(
        displayName = "Void Eclipse",
        colors = listOf(
            Color(0xFFE0C9FF),
            Color(0xFFB566FF),
            Color(0xFF2A0845),
            Color(0xFFB566FF),
            Color(0xFFE0C9FF)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFF9B30FF),
        berriesCost = 5500L,
    ),
    FrostCrown(
        displayName = "Frost Crown",
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFBEE8FF),
            Color(0xFF4FC3F7),
            Color(0xFFBEE8FF),
            Color(0xFFFFFFFF)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFF7FDBFF),
        berriesCost = 5500L,
    ),
    AbyssalTide(
        displayName = "Abyssal Tide",
        colors = listOf(
            Color(0xFFB2FFF5),
            Color(0xFF00C2A8),
            Color(0xFF014D4E),
            Color(0xFF00C2A8),
            Color(0xFFB2FFF5)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFF00C2A8),
        berriesCost = 5500L,
    ),
    CrimsonRegalia(
        displayName = "Crimson Regalia",
        colors = listOf(
            Color(0xFFFFD1D1),
            Color(0xFFFF3B5C),
            Color(0xFF7A0C1E),
            Color(0xFFFF3B5C),
            Color(0xFFFFD1D1)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFFFF3B5C),
        berriesCost = 6000L,
    ),
    InfernoRush(
        displayName = "Inferno Rush",
        colors = listOf(
            Color(0xFFFFF3B0),
            Color(0xFFFF5A1F),
            Color(0xFF7A0C00),
            Color(0xFFFF5A1F),
            Color(0xFFFFF3B0)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFFFF3D00),
        speed = 2.2f,
        berriesCost = 6500L,
    ),
    CrimsonWildfire(
        displayName = "Crimson Wildfire",
        colors = listOf(
            Color(0xFFFFD4B8),
            Color(0xFFFF2D2D),
            Color(0xFF3D0000),
            Color(0xFFFF2D2D),
            Color(0xFFFFD4B8)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFFFF1A1A),
        speed = 2.6f,
        berriesCost = 7000L,
    ),
    GlacialFrost(
        displayName = "Glacial Frost",
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFF8FE3FF),
            Color(0xFF1E6FE0),
            Color(0xFF8FE3FF),
            Color(0xFFFFFFFF)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFF4FC3F7),
        speed = 1f,
        berriesCost = 5500L,
    ),
    EmeraldPulse(
        displayName = "Emerald Pulse",
        colors = listOf(Color(0xFF34D399), Color(0xFF065F46)),
        berriesCost = 1000L,
    ),
    CosmicDrift(
        displayName = "Cosmic Drift",
        colors = listOf(Color(0xFF6366F1), Color(0xFF1E1B4B)),
        berriesCost = 1200L,
    ),
    GoldenHour(
        displayName = "Golden Hour",
        colors = listOf(Color(0xFFFFE08A), Color(0xFFB8860B)),
        berriesCost = 900L,
    ),
    NeonTokyo(
        displayName = "Neon Tokyo",
        colors = listOf(Color(0xFFFF3CAC), Color(0xFF2B86C5)),
        berriesCost = 1100L,
    ),
    StormSurge(
        displayName = "Storm Surge",
        colors = listOf(Color(0xFFB0BEC5), Color(0xFF37474F)),
        berriesCost = 800L,
    ),
    SakuraBloom(
        displayName = "Sakura Bloom",
        colors = listOf(Color(0xFFFFD1E8), Color(0xFFF472B6)),
        berriesCost = 850L,
    ),
    Starlight(
        displayName = "Starlight",
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFD9D9F5),
            Color(0xFFB8B8E8),
            Color(0xFFD9D9F5),
            Color(0xFFFFFFFF)
        ),
        shine = true,
        fireGlow = false,
        speed = 1f,
        berriesCost = 3000L,
    ),
    Prismatic(
        displayName = "Prismatic",
        colors = listOf(
            Color(0xFFFFF3B0),
            Color(0xFFFFFFFF),
            Color(0xFFFF9CEE),
            Color(0xFFB566FF),
            Color(0xFF7FDBFF),
            Color(0xFFFFF3B0)
        ),
        shine = true,
        fireGlow = true,
        glowColor = Color(0xFFFFFFFF),
        speed = 1.4f,
        glowIntensity = 1.8f,
        berriesCost = 15000L,
    ),
}

/**
 * Layers this gradient on top of [base]. [NameGradient.Classic] returns
 * [base] untouched — no override, so the name keeps whatever plain color
 * it already had. Every other tier swaps in a linear gradient brush;
 * [NameGradient.LegendaryBlaze] additionally animates the gradient's
 * position for a moving shine sweep and pulses a warm text shadow for
 * the fire glow.
 */
@Composable
fun NameGradient.textStyle(base: TextStyle): TextStyle {
    if (colors.isEmpty()) return base

    if (!shine) {
        return base.copy(brush = Brush.linearGradient(colors))
    }

    val transition = rememberInfiniteTransition(label = "nameGradientBlaze")
    val sweep by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (2000 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepOffset"
    )
    val flickerBlur by transition.animateFloat(
        initialValue = 8f * glowIntensity,
        targetValue = 18f * glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (550 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flickerBlur"
    )

    return base.copy(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(sweep, 0f),
            end = Offset(sweep + 220f, 0f),
            tileMode = TileMode.Mirror
        ),
        shadow = if (fireGlow) {
            Shadow(
                color = glowColor.copy(alpha = 0.85f),
                offset = Offset(0f, 0f),
                blurRadius = flickerBlur
            )
        } else {
            base.shadow
        }
    )
}
