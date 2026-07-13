package com.example.animetracker.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animetracker.ui.components.VizoraWordmark
import com.example.animetracker.ui.theme.Blaze
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.CharcoalHigh
import com.example.animetracker.ui.theme.Pulse
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.ui.theme.Void
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shown for the brief window between app launch and the initial home feed
 * load finishing. A night-sky scene — twinkling starfield, an occasional
 * shooting star, a pulsing gradient aura with orbiting particles behind the
 * wordmark, and an original ship silhouette rocking on animated waves.
 * Nothing here reproduces any existing show's design.
 */
@Composable
fun SplashScreen() {
    // A single looping clock drives the waves and the ship's bob/tilt.
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "splash")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-phase"
    )
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo-pulse"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring-rotation"
    )
    val shootingStar by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shooting-star"
    )

    // One-shot entrance animation for the logo (fades and grows in on launch).
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    val stars = remember {
        List(55) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.6f,
                radius = Random.nextFloat() * 2f + 0.6f,
                phase = Random.nextFloat() * (2 * Math.PI).toFloat()
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Void
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Deep vertical gradient backdrop, slightly lighter through the middle.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Void, CharcoalHigh.copy(alpha = 0.55f), Void)
                        )
                    )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawStars(stars, twinkle)
                drawShootingStar(shootingStar)
                drawMoon(logoPulse)
                drawWaveLayer(
                    phase = time,
                    color = Blaze.copy(alpha = 0.10f),
                    amplitude = 16f,
                    heightFraction = 0.60f,
                    speed = 0.7f
                )
                drawWaveLayer(
                    phase = time,
                    color = CharcoalHigh,
                    amplitude = 14f,
                    heightFraction = 0.62f,
                    speed = 1f
                )
                drawWaveLayer(
                    phase = time,
                    color = Charcoal,
                    amplitude = 10f,
                    heightFraction = 0.7f,
                    speed = 1.6f
                )
                drawShip(time)
            }

            // Pulsing gradient aura behind the wordmark.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .size(220.dp)
                    .scale(logoPulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Blaze.copy(alpha = 0.32f),
                                Pulse.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Orbiting particles circling the aura.
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .size(220.dp)
            ) {
                rotate(degrees = ringRotation) {
                    repeat(3) { i ->
                        rotate(degrees = i * 120f) {
                            drawCircle(
                                color = Bone.copy(alpha = 0.85f),
                                radius = 3.2f,
                                center = Offset(size.width / 2f, size.height * 0.06f)
                            )
                        }
                    }
                }
            }

            // Wordmark + tagline, fading and growing in on launch.
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .alpha(entrance.value)
                    .scale(0.85f + 0.15f * entrance.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VizoraWordmark(fontSize = 40.sp, markSize = 40.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "STREAM YOUR STORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke,
                    letterSpacing = 3.sp
                )
            }

            // Animated gradient loading dots + status text.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingDots(phase = time)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Loading your anime...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float)

/** Scattered twinkling stars, each fading in and out on its own phase offset. */
private fun DrawScope.drawStars(stars: List<Star>, twinklePhase: Float) {
    stars.forEach { star ->
        val alpha = (0.3f + 0.7f * ((sin(twinklePhase + star.phase) + 1f) / 2f)).coerceIn(0f, 1f)
        drawCircle(
            color = Bone.copy(alpha = alpha),
            radius = star.radius,
            center = Offset(star.x * size.width, star.y * size.height)
        )
    }
}

/** A short fading streak that sweeps across the upper sky on a loop. */
private fun DrawScope.drawShootingStar(progress: Float) {
    if (progress < 0f || progress > 1f) return
    val startX = size.width * 1.1f
    val startY = size.height * 0.08f
    val endX = size.width * -0.1f
    val endY = size.height * 0.40f
    val headX = startX + (endX - startX) * progress
    val headY = startY + (endY - startY) * progress
    val trail = 0.14f
    val tailProgress = (progress - trail).coerceAtLeast(0f)
    val tailX = startX + (endX - startX) * tailProgress
    val tailY = startY + (endY - startY) * tailProgress
    val edgeFade = (1f - progress).coerceIn(0f, 1f).coerceAtMost((progress / trail).coerceIn(0f, 1f))

    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Bone.copy(alpha = edgeFade), Color.Transparent),
            start = Offset(headX, headY),
            end = Offset(tailX, tailY)
        ),
        start = Offset(headX, headY),
        end = Offset(tailX, tailY),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}

/** A glowing Pulse-tinted moon, its halo breathing with the logo pulse. */
private fun DrawScope.drawMoon(glowScale: Float) {
    val center = Offset(size.width * 0.74f, size.height * 0.22f)
    val baseRadius = size.minDimension * 0.11f
    drawCircle(color = Pulse.copy(alpha = 0.12f), radius = baseRadius * 1.8f * glowScale, center = center)
    drawCircle(color = Pulse.copy(alpha = 0.22f), radius = baseRadius * 1.25f, center = center)
    drawCircle(color = Pulse.copy(alpha = 0.4f), radius = baseRadius, center = center)
}

/**
 * One layer of a repeating sine-wave "water" band. Multiple layers at
 * different speeds/amplitudes/colors give a simple parallax feel.
 */
private fun DrawScope.drawWaveLayer(
    phase: Float,
    color: Color,
    amplitude: Float,
    heightFraction: Float,
    speed: Float
) {
    val baseline = size.height * heightFraction
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseline)
        val step = 8f
        var x = 0f
        while (x <= size.width) {
            val y = baseline + amplitude * sin((x / 60f) + phase * speed)
            lineTo(x, y)
            x += step
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color = color)
}

/**
 * A minimal original ship silhouette: hull, mast, single sail, riding the
 * front wave layer. Bobs vertically and tilts slightly with the same clock
 * driving the waves so it reads as "sitting in the water" rather than
 * floating independently of it.
 */
private fun DrawScope.drawShip(phase: Float) {
    val baseline = size.height * 0.62f
    val bob = 6f * sin(phase * 1f)
    val centerX = size.width * 0.5f
    val shipY = baseline + bob - 18f

    val hullWidth = size.width * 0.28f
    val hullHeight = 22f

    // Soft light pooling under the sail.
    drawCircle(
        color = Blaze.copy(alpha = 0.08f),
        radius = hullWidth * 0.9f,
        center = Offset(centerX, shipY - 20f)
    )

    // Hull: a simple rounded trapezoid.
    val hull = Path().apply {
        moveTo(centerX - hullWidth / 2, shipY)
        lineTo(centerX + hullWidth / 2, shipY)
        lineTo(centerX + hullWidth / 2 - 14f, shipY + hullHeight)
        lineTo(centerX - hullWidth / 2 + 14f, shipY + hullHeight)
        close()
    }
    drawPath(hull, color = Smoke)

    // Mast.
    drawLine(
        color = Bone,
        start = Offset(centerX, shipY),
        end = Offset(centerX, shipY - 60f),
        strokeWidth = 4f
    )

    // Sail: a single triangle, tilting gently with the same phase as the bob.
    val tilt = 6f * sin(phase * 1f + 1.2f)
    val sail = Path().apply {
        moveTo(centerX, shipY - 58f)
        lineTo(centerX + 34f + tilt, shipY - 30f)
        lineTo(centerX, shipY - 6f)
        close()
    }
    drawPath(sail, color = Bone.copy(alpha = 0.9f))
}

/** Three gradient dots pulsing out of sync, standing in for a loading spinner. */
@Composable
private fun LoadingDots(phase: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val dotScale = 0.55f + 0.45f * ((sin(phase * 2f + i * 1.4f) + 1f) / 2f)
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .scale(dotScale)
                    .background(
                        Brush.linearGradient(listOf(Blaze, Pulse)),
                        shape = CircleShape
                    )
            )
        }
    }
}
