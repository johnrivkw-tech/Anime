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
        berriesCost = 5000L,
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
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepOffset"
    )
    val flickerBlur by transition.animateFloat(
        initialValue = 8f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
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
                color = Color(0xFFFF6A00).copy(alpha = 0.85f),
                offset = Offset(0f, 0f),
                blurRadius = flickerBlur
            )
        } else {
            base.shadow
        }
    )
}
