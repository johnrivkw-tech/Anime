package com.example.animetracker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animetracker.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.rememberInfiniteTransition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// PALETTE
// ----------------------------------------------------------------------------
// Deliberately cooler/darker than the app's own orange-pink brand palette
// (Blaze / Pulse from ui/theme/Color.kt) so that on this one screen the
// brand colors read as a hot, glowing accent erupting out of a cold void —
// the classic "power awakening" contrast used in AAA key art. The Blaze/
// Pulse hues are reused (not reinvented) here so the splash still feels
// unmistakably like *this* app and not a generic template.
// ============================================================================
private val Void = Color(0xFF060509)
private val VoidDeep = Color(0xFF000000)
private val NebulaViolet = Color(0xFF2A1245)
private val NebulaBlue = Color(0xFF0F1E4A)
private val NebulaTeal = Color(0xFF0A2E3A)
private val EmberOrange = Color(0xFFFF5A1F)   // == Blaze
private val EmberGold = Color(0xFFFFB020)
private val EmberPink = Color(0xFFFF2D6B)     // == Pulse
private val CoreWhite = Color(0xFFFFFDF8)
private val DustBlue = Color(0xFF9FB4FF)
private val MoonPale = Color(0xFFFDEFD9)

// Where the emblem/god-rays/burst effects all converge on screen, as a
// fraction of the full canvas — one shared constant so every layer that
// needs to "aim" at the emblem (rays, gather particles, sparks, speed
// lines) agrees on the exact same point instead of eyeballing it separately.
private val FocalPoint = Offset(0.5f, 0.42f)

/**
 * A cinematic "power awakening" splash: a painterly nebula-and-moon backdrop
 * with slow Ken-Burns camera motion, rotating god-rays, ambient embers and
 * dust drifting at independent parallax speeds, energy particles gathering
 * inward toward a focal point, then an impact beat — flash, screen-shake,
 * an outward spark shower, radial speed-lines, and a chromatic emblem
 * eruption with bloom, a shockwave ring, and a looping shimmer — resolving
 * into a letter-staggered gradient wordmark and a custom energy-ring loader.
 *
 * Everything is procedural vector drawing (Canvas/Path/gradients) — no
 * bitmap assets required, so it's sharp at any density and adds no decode
 * cost on cold start. All transforms ride on `graphicsLayer` so the engine
 * can composite them as GPU layers instead of re-running layout/draw.
 */
@Composable
fun SplashScreen() {
    // ---- Continuous ambient motion (loops forever, independent of entrance) ----
    val ambient = rememberInfiniteTransition(label = "ambient")

    val cameraZoom by ambient.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(tween(14000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "camera-zoom"
    )
    val driftT by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift"
    )
    val rayRotation by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "ray-rotation"
    )
    val emberTime by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "ember-time"
    )
    val dustTime by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "dust-time"
    )
    val corePulse by ambient.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "core-pulse"
    )
    val moonPulse by ambient.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "moon-pulse"
    )
    val shimmer by ambient.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val loaderSweep by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "loader-sweep"
    )

    // ---- One-shot entrance choreography (plays once on composition) ----
    // Beat 1 (0-320ms):   ambient rays fade in; loose energy particles
    //                     gather inward toward the focal point.
    // Beat 2 (320ms):     IMPACT — flash frame, screen-shake, outward
    //                     spark shower, radial speed-lines, the emblem
    //                     erupts in (chromatic ghost -> bloom -> shockwave).
    // Beat 3 (+520ms):    wordmark letters pop in, staggered; tagline fades.
    // Beat 4 (+420ms):    energy-ring loader fades in.
    val emblemScale = remember { Animatable(0.35f) }
    val emblemAlpha = remember { Animatable(0f) }
    val ghostAmount = remember { Animatable(1f) }
    val shockwave = remember { Animatable(0f) }
    val raysAlpha = remember { Animatable(0f) }
    val gatherProgress = remember { Animatable(0f) }
    val burstProgress = remember { Animatable(0f) }
    val linesGrow = remember { Animatable(0f) }
    val linesFade = remember { Animatable(0f) }
    val shakeProgress = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(18f) }
    val titleAlpha = remember { Animatable(0f) }
    val loaderAlpha = remember { Animatable(0f) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }
    var lettersPlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { raysAlpha.animateTo(1f, tween(1400, easing = LinearEasing)) }
        launch { gatherProgress.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }

        delay(320) // ---- IMPACT ----
        flashAlpha = 0.9f
        launch {
            val steps = 10
            for (i in steps downTo 0) {
                flashAlpha = 0.9f * (i / steps.toFloat())
                delay(16)
            }
        }
        launch { shakeProgress.animateTo(1f, tween(260, easing = LinearEasing)) }
        launch { burstProgress.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        launch { linesGrow.animateTo(1f, tween(220, easing = FastOutSlowInEasing)) }
        launch {
            delay(90)
            linesFade.animateTo(1f, tween(420, easing = LinearEasing))
        }
        launch { emblemScale.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 260f)) }
        launch { emblemAlpha.animateTo(1f, tween(260, easing = EaseOutCubic)) }
        launch { ghostAmount.animateTo(0f, tween(650, easing = FastOutSlowInEasing)) }
        launch {
            delay(120)
            shockwave.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
        }

        delay(520)
        launch { titleOffset.animateTo(0f, tween(520, easing = EaseOutCubic)) }
        launch { titleAlpha.animateTo(1f, tween(560, easing = LinearEasing)) }
        lettersPlay = true

        delay(420)
        launch { loaderAlpha.animateTo(1f, tween(500, easing = LinearEasing)) }
    }

    val embers = remember {
        List(26) {
            Ember(
                xFrac = Random.nextFloat(),
                phase = Random.nextFloat(),
                speed = 0.55f + Random.nextFloat() * 0.9f,
                sway = 0.02f + Random.nextFloat() * 0.05f,
                swayFreq = 0.5f + Random.nextFloat() * 1.2f,
                sizePx = 2.5f + Random.nextFloat() * 5.5f,
                warm = Random.nextFloat() < 0.7f,
                depth = 0.4f + Random.nextFloat() * 0.6f
            )
        }
    }
    val dust = remember {
        List(34) {
            DustMote(
                xFrac = Random.nextFloat(),
                yFrac = Random.nextFloat(),
                radius = 1f + Random.nextFloat() * 2.4f,
                phase = Random.nextFloat() * (2f * PI.toFloat()),
                freq = 0.08f + Random.nextFloat() * 0.18f,
                depth = 0.2f + Random.nextFloat() * 0.5f
            )
        }
    }
    val gatherParticles = remember {
        List(14) {
            GatherParticle(
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                startRadiusFrac = 0.34f + Random.nextFloat() * 0.22f,
                sizePx = 2f + Random.nextFloat() * 3f,
                warm = Random.nextFloat() < 0.6f
            )
        }
    }
    val burstSparks = remember {
        List(22) {
            BurstSpark(
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                speedFrac = 0.22f + Random.nextFloat() * 0.34f,
                sizePx = 1.5f + Random.nextFloat() * 3f,
                warm = Random.nextFloat() < 0.65f
            )
        }
    }
    val speedLineAngles = remember {
        List(16) { i -> (360f / 16) * i + Random.nextFloat() * 8f }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ============================================================
        // LAYER 0 — nebula + moon backdrop with camera zoom + parallax pan.
        // ============================================================
        val bgPanX = (sin(driftT * 2f * PI.toFloat()) * 10f)
        val bgPanY = (cos(driftT * 2f * PI.toFloat() * 0.7f) * 6f)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = cameraZoom
                    scaleY = cameraZoom
                    translationX = bgPanX
                    translationY = bgPanY
                }
        ) {
            drawNebulaBackdrop(driftT, moonPulse)
        }

        // ============================================================
        // LAYER 1 — rotating god-rays behind the emblem, mid parallax.
        // ============================================================
        val midPanX = bgPanX * 1.8f
        val midPanY = bgPanY * 1.8f
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = midPanX; translationY = midPanY }
        ) {
            val focal = Offset(size.width * FocalPoint.x, size.height * FocalPoint.y)
            drawGodRays(focal, rayRotation, raysAlpha.value)
        }

        // ============================================================
        // LAYER 2 — rising embers (fast parallax, warm brand-tied color).
        // ============================================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEmbers(embers, emberTime, driftT)
        }

        // ============================================================
        // LAYER 3 — energy gathering inward, then erupting outward as a
        // spark shower + radial speed-lines, all aimed at FocalPoint.
        // ============================================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val focal = Offset(size.width * FocalPoint.x, size.height * FocalPoint.y)
            drawGatherParticles(gatherParticles, gatherProgress.value, focal)
            drawSpeedLines(speedLineAngles, linesGrow.value, linesFade.value, focal)
            drawBurstSparks(burstSparks, burstProgress.value, focal)
        }

        // ============================================================
        // LAYER 4 — ambient dust motes, slowest / nearest parallax.
        // ============================================================
        val nearPanX = bgPanX * 2.6f
        val nearPanY = bgPanY * 2.6f
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = nearPanX; translationY = nearPanY }
        ) {
            drawDustMotes(dust, dustTime)
        }

        // Cinematic vignette, darkening the frame edges toward black.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        center = Offset.Unspecified,
                        radius = 1400f
                    )
                )
        )

        // Impact flash-frame.
        if (flashAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CoreWhite.copy(alpha = flashAlpha * 0.22f))
            )
        }

        // ============================================================
        // FOREGROUND — emblem, wordmark, tagline, energy-ring loader.
        // A short decaying shake rides on this whole block at impact.
        // ============================================================
        val shakeX = sin(shakeProgress.value * 30f) * (1f - shakeProgress.value) * 6f
        val shakeY = cos(shakeProgress.value * 22f) * (1f - shakeProgress.value) * 4f
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { translationX = shakeX; translationY = shakeY },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Breathing bloom halo behind the emblem.
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .graphicsLayer { scaleX = corePulse; scaleY = corePulse; alpha = emblemAlpha.value }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmberGold.copy(alpha = 0.30f),
                                    EmberOrange.copy(alpha = 0.16f),
                                    EmberPink.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Outward shockwave ring — fires once as the emblem lands.
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .graphicsLayer { alpha = (1f - shockwave.value).coerceIn(0f, 1f) }
                ) {
                    if (shockwave.value > 0f) {
                        val r = size.minDimension * 0.28f + size.minDimension * 0.34f * shockwave.value
                        drawCircle(
                            color = CoreWhite,
                            radius = r,
                            center = center,
                            style = Stroke(width = (1f - shockwave.value) * 10f + 1f)
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .size(148.dp)
                        .graphicsLayer {
                            scaleX = emblemScale.value
                            scaleY = emblemScale.value
                            alpha = emblemAlpha.value
                        }
                ) {
                    drawEnergyEmblem(shimmer, ghostAmount.value)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = titleOffset.value
                        alpha = titleAlpha.value
                    }
            ) {
                ShimmeringWordmark(shimmer = shimmer, play = lettersPlay)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "YOUR ANIME UNIVERSE",
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 3.4.sp
                ),
                color = DustBlue.copy(alpha = 0.65f),
                modifier = Modifier.graphicsLayer {
                    translationY = titleOffset.value * 0.6f
                    alpha = titleAlpha.value
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Custom energy-ring loader — rotating gradient arc, not a
            // generic Material CircularProgressIndicator.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { alpha = loaderAlpha.value },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawEnergyRing(loaderSweep)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SYNCING YOUR LIBRARY",
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 10.sp,
                    letterSpacing = 2.4.sp
                ),
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.graphicsLayer { alpha = loaderAlpha.value }
            )
        }
    }
}

// ============================================================================
// PARTICLE / LAYER DATA
// ============================================================================
private data class Ember(
    val xFrac: Float,
    val phase: Float,
    val speed: Float,
    val sway: Float,
    val swayFreq: Float,
    val sizePx: Float,
    val warm: Boolean,
    val depth: Float
)

private data class DustMote(
    val xFrac: Float,
    val yFrac: Float,
    val radius: Float,
    val phase: Float,
    val freq: Float,
    val depth: Float
)

/** A mote drifting from open sky inward toward [FocalPoint] as energy gathers. */
private data class GatherParticle(
    val angle: Float,
    val startRadiusFrac: Float,
    val sizePx: Float,
    val warm: Boolean
)

/** A spark shot outward from [FocalPoint] at impact, with light gravity drift. */
private data class BurstSpark(
    val angle: Float,
    val speedFrac: Float,
    val sizePx: Float,
    val warm: Boolean
)

// ============================================================================
// BACKGROUND
// ============================================================================

/** Deep-space void with a soft painterly moon and drifting nebula blobs. */
private fun DrawScope.drawNebulaBackdrop(t: Float, moonPulse: Float) {
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(Void, VoidDeep)),
        size = size
    )

    // A large, soft moon sitting behind the god-rays gives the frame a
    // single strong focal anchor, the way anime key-art often places a
    // moon or sun directly behind the hero silhouette.
    val moonCenter = Offset(size.width * 0.5f, size.height * 0.30f)
    val moonRadius = size.minDimension * 0.30f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                MoonPale.copy(alpha = 0.16f * moonPulse),
                EmberGold.copy(alpha = 0.07f * moonPulse),
                Color.Transparent
            ),
            center = moonCenter,
            radius = moonRadius
        ),
        radius = moonRadius,
        center = moonCenter
    )
    drawArc(
        color = MoonPale.copy(alpha = 0.10f * moonPulse),
        startAngle = 200f,
        sweepAngle = 260f,
        useCenter = false,
        topLeft = Offset(moonCenter.x - moonRadius * 0.62f, moonCenter.y - moonRadius * 0.62f),
        size = Size(moonRadius * 1.24f, moonRadius * 1.24f),
        style = Stroke(width = 2.5f)
    )

    val blobs = listOf(
        Triple(NebulaViolet, 0.30f, 0.32f) to (0.55f to 0.9f),
        Triple(NebulaBlue, 0.72f, 0.60f) to (0.48f to 1.15f),
        Triple(NebulaTeal, 0.22f, 0.82f) to (0.40f to 0.75f)
    )
    blobs.forEachIndexed { i, (data, motion) ->
        val (color, baseX, baseY) = data
        val (radiusFrac, speedMul) = motion
        val angle = t * 2f * PI.toFloat() * speedMul + i * 2.1f
        val cx = (baseX + sin(angle) * 0.06f) * size.width
        val cy = (baseY + cos(angle * 0.8f) * 0.05f) * size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
                center = Offset(cx, cy),
                radius = size.minDimension * radiusFrac
            ),
            radius = size.minDimension * radiusFrac,
            center = Offset(cx, cy)
        )
    }

    // Faint horizon glow, low on the frame, grounds the composition.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, EmberOrange.copy(alpha = 0.05f), Color.Transparent),
            startY = size.height * 0.75f,
            endY = size.height
        ),
        topLeft = Offset(0f, size.height * 0.75f),
        size = Size(size.width, size.height * 0.25f)
    )
}

/** Slowly-rotating soft god-rays fanning out from behind the emblem. */
private fun DrawScope.drawGodRays(center: Offset, rotationDeg: Float, alpha: Float) {
    if (alpha <= 0.001f) return
    val rayCount = 10
    rotate(degrees = rotationDeg, pivot = center) {
        repeat(rayCount) { i ->
            val sweep = 6f
            val start = (360f / rayCount) * i
            val rayAlpha = alpha * if (i % 2 == 0) 0.08f else 0.045f
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(EmberGold.copy(alpha = rayAlpha), Color.Transparent),
                    center = center,
                    radius = size.maxDimension * 0.75f
                ),
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(center.x - size.maxDimension * 0.75f, center.y - size.maxDimension * 0.75f),
                size = Size(size.maxDimension * 1.5f, size.maxDimension * 1.5f)
            )
        }
    }
}

/** Warm embers/sparks drifting upward with per-particle sway and twinkle. */
private fun DrawScope.drawEmbers(embers: List<Ember>, t: Float, driftT: Float) {
    embers.forEach { e ->
        val progress = (t * e.speed + e.phase) % 1f
        val y = size.height * (1.08f - progress * 1.2f)
        val sway = sin(progress * 2f * PI.toFloat() * e.swayFreq + e.phase * 10f) * e.sway * size.width
        val parallax = sin(driftT * 2f * PI.toFloat()) * 12f * e.depth
        val x = e.xFrac * size.width + sway + parallax
        val fadeIn = (progress / 0.12f).coerceIn(0f, 1f)
        val fadeOut = ((1f - progress) / 0.25f).coerceIn(0f, 1f)
        val twinkle = 0.6f + 0.4f * sin(progress * 40f + e.phase * 30f)
        val alpha = (fadeIn * fadeOut * (0.35f + 0.4f * twinkle) * e.depth).coerceIn(0f, 1f)
        if (alpha <= 0.01f) return@forEach

        val color = if (e.warm) EmberOrange else EmberGold
        val r = e.sizePx * (0.6f + 0.4f * e.depth)
        drawCircle(color = color.copy(alpha = alpha * 0.35f), radius = r * 2.6f, center = Offset(x, y))
        drawCircle(color = CoreWhite.copy(alpha = alpha * 0.85f), radius = r * 0.45f, center = Offset(x, y))
        drawCircle(color = color.copy(alpha = alpha), radius = r, center = Offset(x, y))
    }
}

/** Faint, slow ambient dust — the "nearest" parallax layer. */
private fun DrawScope.drawDustMotes(dust: List<DustMote>, t: Float) {
    dust.forEach { d ->
        val drift = sin(t * 2f * PI.toFloat() * d.freq + d.phase) * 14f * d.depth
        val driftY = cos(t * 2f * PI.toFloat() * d.freq * 0.6f + d.phase) * 8f * d.depth
        val twinkle = 0.5f + 0.5f * ((sin(t * 2f * PI.toFloat() * 2f + d.phase) + 1f) / 2f)
        drawCircle(
            color = DustBlue.copy(alpha = 0.10f * twinkle * d.depth),
            radius = d.radius,
            center = Offset(d.xFrac * size.width + drift, d.yFrac * size.height + driftY)
        )
    }
}

/** Loose energy motes drifting inward from the edges toward [target] as power gathers. */
private fun DrawScope.drawGatherParticles(particles: List<GatherParticle>, progress: Float, target: Offset) {
    if (progress <= 0.001f || progress >= 0.999f) return
    val eased = progress * progress // accelerate inward, like it's being pulled in
    particles.forEach { p ->
        val radius = size.minDimension * p.startRadiusFrac * (1f - eased)
        val x = target.x + cos(p.angle) * radius
        val y = target.y + sin(p.angle) * radius
        // Bright near the end of the pull, faint at the start.
        val alpha = (progress * 0.9f).coerceIn(0f, 0.9f)
        val color = if (p.warm) EmberGold else DustBlue
        drawCircle(color = color.copy(alpha = alpha * 0.4f), radius = p.sizePx * 2f, center = Offset(x, y))
        drawCircle(color = color.copy(alpha = alpha), radius = p.sizePx * 0.6f, center = Offset(x, y))
    }
}

/** Bright sparks shot outward from [target] at impact, with a light gravity arc. */
private fun DrawScope.drawBurstSparks(sparks: List<BurstSpark>, progress: Float, target: Offset) {
    if (progress <= 0.001f) return
    val alpha = (1f - progress).coerceIn(0f, 1f)
    if (alpha <= 0.01f) return
    sparks.forEach { s ->
        val dist = size.minDimension * s.speedFrac * progress
        val gravity = size.minDimension * 0.10f * progress * progress
        val x = target.x + cos(s.angle) * dist
        val y = target.y + sin(s.angle) * dist + gravity
        val color = if (s.warm) EmberOrange else EmberGold
        val r = s.sizePx * (1f - progress * 0.5f)
        drawCircle(color = color.copy(alpha = alpha * 0.4f), radius = r * 2.4f, center = Offset(x, y))
        drawCircle(color = CoreWhite.copy(alpha = alpha), radius = r * 0.5f, center = Offset(x, y))
    }
}

/** Radial manga-style speed-lines that snap out from [target] at impact and quickly fade. */
private fun DrawScope.drawSpeedLines(angles: List<Float>, grow: Float, fade: Float, target: Offset) {
    if (grow <= 0.001f) return
    val alpha = (1f - fade).coerceIn(0f, 1f)
    if (alpha <= 0.01f) return
    val innerR = size.minDimension * 0.11f
    val maxLen = size.minDimension * 0.30f
    angles.forEachIndexed { i, deg -> 
        val rad = deg * (PI.toFloat() / 180f)
        val len = maxLen * grow
        val dir = Offset(cos(rad), sin(rad))
        val start = Offset(target.x + dir.x * innerR, target.y + dir.y * innerR)
        val end = Offset(target.x + dir.x * (innerR + len), target.y + dir.y * (innerR + len))
        val lineAlpha = alpha * if (i % 2 == 0) 0.35f else 0.20f
        drawLine(
            color = CoreWhite.copy(alpha = lineAlpha),
            start = start,
            end = end,
            strokeWidth = if (i % 2 == 0) 2.2f else 1.2f,
            cap = StrokeCap.Round
        )
    }
}

// ============================================================================
// EMBLEM
// ============================================================================

/**
 * The mark: a faceted diamond frame — the same silhouette language as
 * [com.example.animetracker.ui.components.ReiWordmark]'s header badge,
 * scaled up and dramatized — cut through by a bold lightning-bolt shard.
 * The bolt carries the bloom, the chromatic convergence ghost, and the
 * looping [PathMeasure]-driven shimmer; the diamond is a quieter framing
 * device around it, tying the splash mark back to the in-app brand badge.
 */
private fun DrawScope.drawEnergyEmblem(shimmerProgress: Float, ghostAmount: Float) {
    val w = size.width
    val h = size.height

    val diamond = Path().apply {
        moveTo(w * 0.5f, h * 0.05f)
        lineTo(w * 0.92f, h * 0.5f)
        lineTo(w * 0.5f, h * 0.95f)
        lineTo(w * 0.08f, h * 0.5f)
        close()
    }
    val bolt = Path().apply {
        moveTo(w * 0.58f, h * 0.16f)
        lineTo(w * 0.38f, h * 0.50f)
        lineTo(w * 0.58f, h * 0.50f)
        lineTo(w * 0.40f, h * 0.84f)
    }

    // Quiet diamond frame, fixed in place (no ghost/shimmer of its own).
    drawPath(
        diamond,
        color = DustBlue.copy(alpha = 0.35f),
        style = Stroke(width = w * 0.02f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val boltStrokeW = w * 0.115f

    // Chromatic convergence ghosts on the bolt — cyan/pink copies sliding
    // into register as the mark "settles" into focus.
    if (ghostAmount > 0.001f) {
        val offset = 9f * ghostAmount
        translate(left = -offset, top = 0f) {
            drawPath(
                bolt,
                color = Color(0xFF34D6FF).copy(alpha = 0.55f * ghostAmount),
                style = Stroke(width = boltStrokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        translate(left = offset, top = 0f) {
            drawPath(
                bolt,
                color = EmberPink.copy(alpha = 0.55f * ghostAmount),
                style = Stroke(width = boltStrokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    // Bloom layers on the bolt, largest/faintest first — a stand-in for
    // blur that needs no RenderEffect, so it behaves identically back to
    // minSdk 26.
    listOf(
        w * 0.46f to EmberPink.copy(alpha = 0.10f),
        w * 0.32f to EmberOrange.copy(alpha = 0.20f),
        w * 0.22f to EmberGold.copy(alpha = 0.30f)
    ).forEach { (sw, color) ->
        drawPath(bolt, color = color, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    drawPath(
        bolt,
        color = CoreWhite,
        style = Stroke(width = boltStrokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Shimmer segment sliding along the bolt's actual path.
    val measure = PathMeasure()
    measure.setPath(bolt, false)
    val length = measure.length
    if (length > 0f) {
        val segment = length * 0.28f
        val start = (shimmerProgress * (length + segment) - segment).coerceIn(0f, length)
        val end = (start + segment).coerceIn(0f, length)
        if (end > start) {
            val seg = Path()
            measure.getSegment(start, end, seg, true)
            drawPath(
                seg,
                color = CoreWhite.copy(alpha = 0.9f),
                style = Stroke(width = boltStrokeW * 1.05f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Gradient "REI" wordmark, revealed one letter at a time (spring scale +
 * fade, ~70ms stagger) once [play] flips true, with a looping light-sweep
 * highlight over the whole word. The parent Box renders to an offscreen
 * layer ([CompositingStrategy.Offscreen]) so the overlay's [BlendMode.Plus]
 * only lightens pixels already painted by the letters beneath it.
 */
@Composable
private fun ShimmeringWordmark(shimmer: Float, play: Boolean) {
    val letters = listOf("R" to EmberGold, "E" to EmberOrange, "I" to EmberPink)
    val scales = remember { letters.map { Animatable(0.3f) } }
    val alphas = remember { letters.map { Animatable(0f) } }

    LaunchedEffect(play) {
        if (!play) return@LaunchedEffect
        letters.indices.forEach { i ->
            launch { scales[i].animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f)) }
            launch { alphas[i].animateTo(1f, tween(200, easing = EaseOutCubic)) }
            delay(70)
        }
    }

    Box(
        modifier = Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        Row {
            letters.forEachIndexed { i, (letter, color) ->
                Text(
                    text = letter,
                    style = TextStyle(
                        color = color,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                        letterSpacing = 4.sp
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = scales[i].value
                        scaleY = scales[i].value
                        alpha = alphas[i].value
                    }
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            val barWidth = size.width * 0.35f
            val centerX = (shimmer * 1.7f - 0.35f) * size.width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.9f),
                        Color.Transparent
                    ),
                    start = Offset(centerX - barWidth / 2f, 0f),
                    end = Offset(centerX + barWidth / 2f, 0f)
                ),
                blendMode = BlendMode.Plus
            )
        }
    }
}

// ============================================================================
// LOADER
// ============================================================================

/** A rotating, glowing energy-ring — the app's own loader, not a system spinner. */
private fun DrawScope.drawEnergyRing(rotationDeg: Float) {
    val strokeW = size.minDimension * 0.14f
    val radius = (size.minDimension - strokeW) / 2f
    rotate(degrees = rotationDeg, pivot = center) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    EmberGold.copy(alpha = 0.15f),
                    EmberOrange,
                    EmberPink,
                    Color.Transparent
                )
            ),
            startAngle = 0f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
    drawCircle(
        color = Color.White.copy(alpha = 0.08f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeW * 0.4f)
    )
}
