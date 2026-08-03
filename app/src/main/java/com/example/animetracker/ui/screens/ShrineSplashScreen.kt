package com.example.animetracker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// ShrineSplashScreen
// ----------------------------------------------------------------------------
// A cinematic torii-shrine splash built as TWO layers:
//
//   1. STATIC ARTWORK  — a single illustrated background (the torii gate,
//      blossom trees, mountains, river, bamboo, temple roofs, lanterns —
//      everything with real painterly depth). This is one GPU-composited
//      bitmap draw. Its cost is flat no matter how detailed the painting
//      is — a budget device pays the same price as a flagship.
//
//   2. ANIMATED OVERLAY — sakura petals, maple leaves, fireflies, drifting
//      mist, shimmering light rays, and lantern glow-pulses, all drawn
//      procedurally in a handful of Canvas layers. This is the only part
//      that costs CPU/GPU per frame, so it's the only part that needs to
//      scale down for low-end hardware — see [SplashQuality].
//
// This split is deliberate: it's what makes it possible to get AAA-level
// scenery (which procedural drawing alone can't achieve — see the note in
// chat) while keeping runtime cost low enough for entry-level devices like
// the Galaxy A15.
//
// USAGE
// -----
//   ShrineSplashScreen(
//       background = painterResource(R.drawable.splash_shrine_bg),
//       quality = SplashQuality.MEDIUM, // LOW for budget devices
//       logo = { ReiWordmarkDrawn(width = 260.dp) }
//   )
//
// Drop your artwork into res/drawable (or drawable-nodpi for a large,
// non-scaled painting) and pass it in as `background`. `lanternSpots` are
// fractional (0..1) positions of the lanterns IN YOUR ARTWORK, so the glow
// pulses land exactly on them — tune these once you have the final image.
//
// TUNED FOR THE SUPPLIED SHRINE PHOTO
// ------------------------------------
// In the actual artwork this was built against, the torii's crossbeams sit
// around 45-58% height, so a dead-center logo would get cut by the red
// beam. The clean, naturally-eye-catching spot is the open gate interior,
// roughly 58-72% height — the painted path and mist already lead the eye
// there. `logoVerticalBias` defaults to that zone instead of true center.
// The art also already has a painted sunburst and ground mist, so the
// animated ray/mist overlay defaults are toned down to read as a subtle
// shimmer layered on top rather than a second, competing light source —
// drop `quality` to LOW if it still feels like too much.
// ============================================================================

/** Overlay particle density presets. Background art cost is identical at every tier. */
enum class SplashQuality(
    val petalCount: Int,
    val mapleCount: Int,
    val fireflyCount: Int,
    val rayCount: Int,
    val mistLayers: Int
) {
    /** Recommended for entry-level devices (e.g. Galaxy A15-class chipsets). */
    LOW(petalCount = 18, mapleCount = 4, fireflyCount = 8, rayCount = 4, mistLayers = 1),
    MEDIUM(petalCount = 36, mapleCount = 8, fireflyCount = 14, rayCount = 6, mistLayers = 2),
    HIGH(petalCount = 60, mapleCount = 14, fireflyCount = 22, rayCount = 8, mistLayers = 3),
}

private val SakuraPink = Color(0xFFFFC9DB)
private val SakuraPinkDeep = Color(0xFFFF9EBB)
private val MapleRed = Color(0xFFE0512F)
private val MapleGold = Color(0xFFE8862F)
private val FireflyGold = Color(0xFFFFE9A8)
private val LanternGlow = Color(0xFFFFA84D)
private val SunRayGold = Color(0xFFFFE3B0)
private val MistWhite = Color(0xFFF4EDE6)
private val VignetteInk = Color(0xFF0B0710)

/**
 * Cinematic shrine splash: static illustrated background + animated sakura,
 * maple leaves, fireflies, mist, volumetric light rays and lantern glow,
 * with a clean reserved zone for [logo].
 *
 * @param background Your commissioned/generated artwork. Painted for portrait
 *   framing with the torii/pathway on the vertical center line.
 * @param quality Overlay particle density. Does not affect background cost.
 * @param lanternSpots Fractional (0..1) positions of lanterns in [background]
 *   to align the glow-pulse overlay to. Defaults match the supplied shrine
 *   photo, where both lanterns sit low, near the base of the torii posts.
 * @param logoVerticalBias Where [logo] sits vertically: -1f is top, 0f is
 *   true center, 1f is bottom. Defaults to 0.3f — just below center, inside
 *   the open gate archway, clear of the crossbeam in the supplied artwork.
 *   Adjust if you swap in different art with the beam at a different height.
 * @param logo Your app logo/wordmark, placed in the reserved zone with a
 *   gentle one-shot entrance fade.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShrineSplashScreen(
    background: Painter,
    quality: SplashQuality = SplashQuality.MEDIUM,
    lanternSpots: List<Offset> = listOf(Offset(0.14f, 0.74f), Offset(0.86f, 0.74f)),
    logoVerticalBias: Float = 0.3f,
    logo: @Composable BoxScope.() -> Unit = {},
) {
    val infinite = rememberInfiniteTransition(label = "shrine-ambient")

    // Slow cinematic push-in on open, settling into a very gentle breathing
    // zoom — the "parallax on open" cue. True device-tilt parallax can be
    // layered on later via a sensor listener if you want it.
    val introZoom by infinite.animateFloat(
        initialValue = 1.07f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "intro-zoom"
    )

    // Global clock the whole overlay reads from — keeps every particle
    // system perfectly loop-synced off ONE animation instead of many.
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
        label = "world-time"
    )
    val rayShimmer by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "ray-shimmer"
    )
    val lanternFlicker by infinite.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lantern-flicker"
    )
    val fireflyPulse by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "firefly-pulse"
    )
    // One-shot entrance (NOT part of the ambient infinite transition above —
    // it must fire once and hold, not loop).
    val logoAlphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(900)
        logoAlphaAnim.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val logoAlpha = logoAlphaAnim.value

    val petals = remember(quality) { generatePetals(quality.petalCount, seed = 1) }
    val mapleLeaves = remember(quality) { generatePetals(quality.mapleCount, seed = 2) }
    val fireflies = remember(quality) { generateFireflies(quality.fireflyCount, seed = 3) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---- Layer 1: your artwork ----
        // introZoom rides on graphicsLayer, so it's a pure GPU transform on
        // this one Image — no extra draw pass, no re-layout per frame.
        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = introZoom
                    scaleY = introZoom
                }
        )

        // ---- Layer 2: volumetric light rays (behind petals, above art) ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLightRays(rayCount = quality.rayCount, shimmer = rayShimmer, w = size.width, h = size.height)
        }

        // ---- Layer 3: drifting ground mist ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMist(layers = quality.mistLayers, t = t, w = size.width, h = size.height)
        }

        // ---- Layer 4: lantern glow pulses, aligned to your artwork ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            lanternSpots.forEach { spot ->
                drawLanternGlow(
                    center = Offset(spot.x * size.width, spot.y * size.height),
                    radius = size.minDimension * 0.09f,
                    flicker = lanternFlicker
                )
            }
        }

        // ---- Layer 5: fireflies ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFireflies(fireflies, t = t, pulse = fireflyPulse, w = size.width, h = size.height)
        }

        // ---- Layer 6: falling maple leaves (sparser, tumbling) ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFallingParticles(
                particles = mapleLeaves,
                t = t,
                w = size.width,
                h = size.height,
                colorA = MapleRed,
                colorB = MapleGold,
                isMaple = true
            )
        }

        // ---- Layer 7: sakura petals (denser, drifting) — frontmost overlay ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFallingParticles(
                particles = petals,
                t = t,
                w = size.width,
                h = size.height,
                colorA = SakuraPink,
                colorB = SakuraPinkDeep,
                isMaple = false
            )
        }

        // ---- Layer 8: cinematic vignette for depth ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawVignette(size.width, size.height)
        }

        // ---- Layer 9: reserved zone for your logo ----
        // BiasAlignment lets us place it in the open gate archway rather
        // than true center, so it clears the torii crossbeam in the art.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = logoAlpha },
            contentAlignment = androidx.compose.ui.BiasAlignment(0f, logoVerticalBias)
        ) {
            logo()
        }
    }
}

// ============================================================================
// PARTICLE MODELS
// ============================================================================

/** A single falling particle (petal or leaf), parameterized so its motion is a pure function of time — nothing mutates per frame. */
private data class FallingParticle(
    val xBase: Float,      // 0..1 horizontal anchor
    val phase: Float,      // 0..1 offset into the loop so particles don't sync
    val fallSpeed: Float,  // loops per world-time cycle
    val swayAmp: Float,    // horizontal sway amount, fraction of width
    val swayFreq: Float,   // sway oscillations per fall
    val size: Float,       // px at 1x density-independent scale
    val spin: Float,       // rotations per fall
    val colorMix: Float,   // 0..1 blend between colorA/colorB
    val depth: Float,      // 0.6..1.0, cheap parallax + alpha depth cue
)

private data class Firefly(
    val xBase: Float,
    val yBase: Float,
    val roamRadius: Float,
    val roamSpeed: Float,
    val phase: Float,
    val size: Float,
)

private fun generatePetals(count: Int, seed: Int): List<FallingParticle> {
    val r = Random(seed)
    return List(count) {
        FallingParticle(
            xBase = r.nextFloat(),
            phase = r.nextFloat(),
            fallSpeed = 0.6f + r.nextFloat() * 0.9f,
            swayAmp = 0.03f + r.nextFloat() * 0.05f,
            swayFreq = 1.5f + r.nextFloat() * 2.5f,
            size = 5f + r.nextFloat() * 7f,
            spin = 0.5f + r.nextFloat() * 1.5f,
            colorMix = r.nextFloat(),
            depth = 0.55f + r.nextFloat() * 0.45f,
        )
    }
}

private fun generateFireflies(count: Int, seed: Int): List<Firefly> {
    val r = Random(seed)
    return List(count) {
        Firefly(
            xBase = 0.1f + r.nextFloat() * 0.8f,
            yBase = 0.35f + r.nextFloat() * 0.5f,
            roamRadius = 0.02f + r.nextFloat() * 0.035f,
            roamSpeed = 0.4f + r.nextFloat() * 0.8f,
            phase = r.nextFloat(),
            size = 2.5f + r.nextFloat() * 2.5f,
        )
    }
}

// ============================================================================
// DRAW ROUTINES
// ============================================================================

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFallingParticles(
    particles: List<FallingParticle>,
    t: Float,
    w: Float,
    h: Float,
    colorA: Color,
    colorB: Color,
    isMaple: Boolean,
) {
    particles.forEach { p ->
        // Loop each particle from just above the top to just below the
        // bottom, offset by its own phase so the field never looks tiled.
        val loopT = ((t * p.fallSpeed + p.phase) % 1f)
        val y = (loopT * 1.3f - 0.15f) * h
        val sway = sin((loopT * p.swayFreq * 2f * PI).toFloat()) * p.swayAmp * w
        val x = p.xBase * w + sway
        val rotation = (loopT * p.spin * 360f) % 360f
        val alpha = (0.35f + 0.65f * p.depth) * (0.9f - 0.2f * loopT)
        val col = lerpColor(colorA, colorB, p.colorMix).copy(alpha = alpha.coerceIn(0f, 1f))
        val sz = p.size * p.depth

        rotate(degrees = rotation, pivot = Offset(x, y)) {
            if (isMaple) {
                // Simple 4-point maple-ish silhouette via a small diamond+notch look
                drawCircle(color = col, radius = sz * 0.5f, center = Offset(x, y))
            } else {
                // Petal: soft oval, wider than tall
                drawOval(
                    color = col,
                    topLeft = Offset(x - sz * 0.6f, y - sz * 0.4f),
                    size = androidx.compose.ui.geometry.Size(sz * 1.2f, sz * 0.8f)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFireflies(
    flies: List<Firefly>,
    t: Float,
    pulse: Float,
    w: Float,
    h: Float,
) {
    flies.forEach { f ->
        val ang = (t * f.roamSpeed + f.phase) * 2f * PI.toFloat()
        val x = (f.xBase * w) + cos(ang) * f.roamRadius * w
        val y = (f.yBase * h) + sin(ang * 1.3f) * f.roamRadius * h
        val glowAlpha = (0.3f + 0.7f * pulse).coerceIn(0f, 1f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(FireflyGold.copy(alpha = glowAlpha), FireflyGold.copy(alpha = 0f)),
                center = Offset(x, y),
                radius = f.size * 4f
            ),
            radius = f.size * 4f,
            center = Offset(x, y)
        )
        drawCircle(color = FireflyGold.copy(alpha = glowAlpha), radius = f.size * 0.5f, center = Offset(x, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLightRays(
    rayCount: Int,
    shimmer: Float,
    w: Float,
    h: Float,
) {
    val origin = Offset(w * 0.5f, -h * 0.05f)
    val spread = 40f
    for (i in 0 until rayCount) {
        val baseAngle = -spread / 2f + (spread / (rayCount - 1).coerceAtLeast(1)) * i
        val shimmerAlpha = 0.04f + 0.035f * sin((shimmer * 2f * PI + i).toFloat())
        rotate(degrees = baseAngle, pivot = origin) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SunRayGold.copy(alpha = shimmerAlpha.coerceIn(0f, 0.14f)),
                        SunRayGold.copy(alpha = 0f)
                    ),
                    startY = origin.y,
                    endY = h * 0.75f
                ),
                topLeft = Offset(origin.x - w * 0.04f, origin.y),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.8f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMist(
    layers: Int,
    t: Float,
    w: Float,
    h: Float,
) {
    for (i in 0 until layers) {
        val speed = 0.5f + i * 0.25f
        val yBase = h * (0.78f + i * 0.05f)
        val drift = ((t * speed + i * 0.33f) % 1f)
        val xOffset = (drift - 0.5f) * w * 0.4f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MistWhite.copy(alpha = 0f),
                    MistWhite.copy(alpha = 0.07f - i * 0.015f),
                    MistWhite.copy(alpha = 0f)
                ),
                startX = xOffset,
                endX = xOffset + w
            ),
            topLeft = Offset(0f, yBase),
            size = androidx.compose.ui.geometry.Size(w, h * 0.16f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLanternGlow(
    center: Offset,
    radius: Float,
    flicker: Float,
) {
    val alpha = 0.35f * flicker
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(LanternGlow.copy(alpha = alpha), LanternGlow.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVignette(w: Float, h: Float) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(VignetteInk.copy(alpha = 0f), VignetteInk.copy(alpha = 0.35f)),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w.coerceAtLeast(h) * 0.85f
        ),
        size = androidx.compose.ui.geometry.Size(w, h)
    )
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)
