package com.example.animetracker.ui.model

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A decorative ring drawn around the profile avatar (Settings header card
 * and the Profile screen). [ringColors] feeds a linear gradient border;
 * an empty list means "use the live theme's primary/secondary" instead of
 * fixed colors, which is how [Classic] stays in sync with whatever accent
 * is currently selected rather than looking stale next to it.
 *
 * [glow] tiers additionally render a soft, pulsing radial halo behind the
 * avatar via [AvatarGlowHalo] — [haloSize] controls how far it reaches
 * past the ring and [glowIntensity] scales its blur softness and peak
 * brightness. The two flagship tiers ([PhoenixHalo], [CelestialHalo]) push
 * both well past every other glow tier.
 */
enum class AvatarFrame(
    val displayName: String,
    val ringColors: List<Color>,
    val glow: Boolean = false,
    val glowColor: Color = Color.Transparent,
    val haloSize: Dp = 20.dp,
    val glowIntensity: Float = 1f,
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
        glowColor = Color(0xFF9B30FF),
        haloSize = 18.dp,
        berriesCost = 1800L,
    ),
    CoralReef(
        displayName = "Coral Reef",
        ringColors = listOf(Color(0xFFFF8A65), Color(0xFF00BFA5)),
        berriesCost = 1100L,
    ),
    SakuraRing(
        displayName = "Sakura Ring",
        ringColors = listOf(Color(0xFFFFD1E8), Color(0xFFF472B6)),
        berriesCost = 1200L,
    ),
    ObsidianEdge(
        displayName = "Obsidian Edge",
        ringColors = listOf(Color(0xFFB0B0C3), Color(0xFF15131F)),
        berriesCost = 1500L,
    ),
    SolarCrown(
        displayName = "Solar Crown",
        ringColors = listOf(Color(0xFFFFF3B0), Color(0xFFFF7A00)),
        glow = true,
        glowColor = Color(0xFFFFB020),
        haloSize = 20.dp,
        berriesCost = 3500L,
    ),
    PhoenixHalo(
        displayName = "Phoenix Halo",
        ringColors = listOf(Color(0xFFFFE08A), Color(0xFFFF5A1F), Color(0xFF7A0C00)),
        glow = true,
        glowColor = Color(0xFFFF3D00),
        haloSize = 40.dp,
        glowIntensity = 1.9f,
        berriesCost = 10000L,
    ),
    CelestialHalo(
        displayName = "Celestial Halo",
        ringColors = listOf(Color(0xFFFFFFFF), Color(0xFFB566FF), Color(0xFF7FDBFF)),
        glow = true,
        glowColor = Color(0xFFE0C9FF),
        haloSize = 40.dp,
        glowIntensity = 1.9f,
        berriesCost = 10000L,
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

/**
 * Soft pulsing radial glow rendered behind the avatar for [AvatarFrame.glow]
 * tiers — call as the first child inside the same [Box] that holds the
 * avatar [androidx.compose.material3.Surface] so it sits underneath. No-op
 * for non-glow frames. [avatarSize] is the avatar's own diameter; the halo
 * is drawn [AvatarFrame.haloSize] wider than that on every side.
 */
@Composable
fun BoxScope.AvatarGlowHalo(frame: AvatarFrame, avatarSize: Dp) {
    if (!frame.glow) return

    val transition = rememberInfiniteTransition(label = "avatarGlow")
    val pulse by transition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.30f + 0.35f * frame.glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarGlowPulse"
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(avatarSize + frame.haloSize * 2)
            .blur(frame.haloSize * frame.glowIntensity / 2)
            .background(
                Brush.radialGradient(
                    listOf(frame.glowColor.copy(alpha = pulse), Color.Transparent)
                ),
                CircleShape
            )
    )
}
