package com.example.animetracker.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.animetracker.ui.theme.InterFontFamily
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// Local palette for this screen only — deliberately cooler and more
// restrained than the app's orange/pink brand colors so the glowing V
// reads as the one accent on screen, not one of several.
private val MidnightTop = Color(0xFF0B1020)
private val MidnightBottom = Color(0xFF000000)
private val GlowBlue = Color(0xFF5B8CFF)
private val GlowPurple = Color(0xFF8A6CFF)
private val InkRed = Color(0xFFB33A3A)
private val SunRed = Color(0xFFD9333F)
private val PetalPink = Color(0xFFF3B8CC)
private val MistBlue = Color(0xFFC7D2FF)

/**
 * A calm, cinematic splash inspired by traditional Japanese motifs: drifting
 * sakura petals over a midnight gradient, a faint ensō-style brushstroke sun
 * behind a glowing white "V," a hairline loading bar, and a soft shimmer
 * that travels along the V's stroke every few seconds. Nothing here is a
 * reproduction of any real flag, brand, or existing show's artwork.
 */
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Petals falling top to bottom, one shared clock, staggered per-petal.
    val fallTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall-time"
    )
    // Slow ambient drift for the mist particles.
    val mistTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mist-time"
    )
    // Gentle breathing scale on the aura behind the V.
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura-pulse"
    )
    // Shimmer traveling along the V's stroke, looping every few seconds.
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.25f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    // Thin loading line filling left to right, on a loop.
    val lineFill by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line-fill"
    )

    val petals = remember {
        List(16) {
            Petal(
                xFrac = Random.nextFloat(),
                phase = Random.nextFloat(),
                swayAmp = 0.03f + Random.nextFloat() * 0.05f,
                swayFreq = 0.6f + Random.nextFloat() * 0.8f,
                swayPhase = Random.nextFloat() * (2f * PI.toFloat()),
                sizePx = 7f + Random.nextFloat() * 8f,
                rotSpeed = 0.6f + Random.nextFloat() * 1.4f,
                white = Random.nextFloat() < 0.3f
            )
        }
    }
    val mistParticles = remember {
        List(14) {
            MistParticle(
                xFrac = Random.nextFloat(),
                yFrac = 0.80f + Random.nextFloat() * 0.16f,
                freq = 0.15f + Random.nextFloat() * 0.25f,
                phase = Random.nextFloat() * (2f * PI.toFloat()),
                driftAmp = 0.02f + Random.nextFloat() * 0.03f,
                radius = 10f + Random.nextFloat() * 22f,
                baseAlpha = 0.04f + Random.nextFloat() * 0.06f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(MidnightTop, MidnightBottom)))
    ) {
        // Sun, brushstroke ring, petals, and mist all live on one scene canvas.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sunCenter = Offset(size.width * 0.5f, size.height * 0.45f)
            val sunRadius = size.minDimension * 0.20f
            drawCircle(color = SunRed.copy(alpha = 0.10f), radius = sunRadius, center = sunCenter)
            drawBrushstrokeRing(sunCenter, sunRadius)
            drawPetals(petals, fallTime)
            drawMistParticles(mistParticles, mistTime)
            drawMistBand()
        }

        // Cinematic vignette — darkens the edges slightly.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)),
                        radius = 1500f
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Soft blue-purple aura, breathing gently behind the V.
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GlowPurple.copy(alpha = 0.22f * auraPulse),
                                    GlowBlue.copy(alpha = 0.10f * auraPulse),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Canvas(modifier = Modifier.size(128.dp)) {
                    drawVLogo(shimmerProgress)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Hairline loading indicator.
            Box(
                modifier = Modifier
                    .width(148.dp)
                    .height(1.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .width(148.dp * lineFill)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Loading...",
                style = TextStyle(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                ),
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

private data class Petal(
    val xFrac: Float,
    val phase: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val swayPhase: Float,
    val sizePx: Float,
    val rotSpeed: Float,
    val white: Boolean
)

private data class MistParticle(
    val xFrac: Float,
    val yFrac: Float,
    val freq: Float,
    val phase: Float,
    val driftAmp: Float,
    val radius: Float,
    val baseAlpha: Float
)

/** A delicate, hand-painted-looking ring behind the sun — an ensō, not a perfect circle. */
private fun DrawScope.drawBrushstrokeRing(center: Offset, baseRadius: Float) {
    val strokes = listOf(
        Triple(-8f, 328f, 0.10f) to (baseRadius * 1.02f to 3.5f),
        Triple(14f, 300f, 0.18f) to (baseRadius * 0.98f to 6f),
        Triple(4f, 316f, 0.14f) to (baseRadius * 1.06f to 2.5f)
    )
    strokes.forEach { (angleData, radiusData) ->
        val (startAngle, sweep, alpha) = angleData
        val (radius, strokeWidth) = radiusData
        drawArc(
            color = InkRed.copy(alpha = alpha),
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/** Slow-falling sakura petals, drifting side to side, fading in and out at the edges. */
private fun DrawScope.drawPetals(petals: List<Petal>, t: Float) {
    petals.forEach { p ->
        val progress = (t + p.phase) % 1f
        val y = -0.06f * size.height + progress * 1.18f * size.height
        val x = p.xFrac * size.width +
            sin(progress * 2f * PI.toFloat() * p.swayFreq + p.swayPhase) * p.swayAmp * size.width
        val fadeIn = (progress / 0.08f).coerceIn(0f, 1f)
        val fadeOut = ((1f - progress) / 0.15f).coerceIn(0f, 1f)
        val alpha = fadeIn * fadeOut * 0.7f
        if (alpha <= 0.01f) return@forEach

        val color = if (p.white) Color.White.copy(alpha = alpha * 0.75f) else PetalPink.copy(alpha = alpha)
        val rotation = progress * 360f * p.rotSpeed
        rotate(degrees = rotation, pivot = Offset(x, y)) {
            val petalPath = Path().apply {
                moveTo(x, y - p.sizePx)
                cubicTo(x + p.sizePx * 0.6f, y - p.sizePx * 0.6f, x + p.sizePx * 0.6f, y + p.sizePx * 0.6f, x, y + p.sizePx)
                cubicTo(x - p.sizePx * 0.6f, y + p.sizePx * 0.6f, x - p.sizePx * 0.6f, y - p.sizePx * 0.6f, x, y - p.sizePx)
                close()
            }
            drawPath(petalPath, color = color)
        }
    }
}

/** Faint drifting motes within the mist band near the bottom. */
private fun DrawScope.drawMistParticles(particles: List<MistParticle>, t: Float) {
    particles.forEach { m ->
        val drift = sin(t * 2f * PI.toFloat() * m.freq + m.phase) * m.driftAmp * size.width
        val twinkle = 0.6f + 0.4f * ((sin(t * 2f * PI.toFloat() + m.phase) + 1f) / 2f)
        drawCircle(
            color = MistBlue.copy(alpha = m.baseAlpha * twinkle),
            radius = m.radius,
            center = Offset(m.xFrac * size.width + drift, m.yFrac * size.height)
        )
    }
}

/** A soft, layered fog band hugging the bottom edge of the screen. */
private fun DrawScope.drawMistBand() {
    val bandTop = size.height * 0.80f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, MistBlue.copy(alpha = 0.07f)),
            startY = bandTop,
            endY = size.height
        ),
        topLeft = Offset(0f, bandTop),
        size = Size(size.width, size.height - bandTop)
    )
}

/**
 * The stylized "V" mark: a layered blue-purple glow beneath a crisp white
 * chevron, with a bright highlight traveling along the stroke's length —
 * driven by [PathMeasure] so the shimmer follows the actual glyph shape
 * rather than a rectangular mask.
 */
private fun DrawScope.drawVLogo(shimmerProgress: Float) {
    val w = size.width
    val h = size.height
    val vPath = Path().apply {
        moveTo(w * 0.16f, h * 0.14f)
        lineTo(w * 0.5f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.14f)
    }

    // Outer glow layers, largest/faintest first.
    listOf(
        (w * 0.40f) to GlowPurple.copy(alpha = 0.10f),
        (w * 0.28f) to GlowBlue.copy(alpha = 0.18f),
        (w * 0.20f) to GlowBlue.copy(alpha = 0.28f)
    ).forEach { (strokeWidth, color) ->
        drawPath(
            vPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    val coreStrokeWidth = w * 0.115f
    drawPath(
        vPath,
        color = Color.White,
        style = Stroke(width = coreStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Shimmer: a short bright segment sliding along the V's own path.
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(vPath, false)
    val length = pathMeasure.length
    if (length > 0f) {
        val segmentLength = length * 0.22f
        val start = shimmerProgress * (length + segmentLength) - segmentLength
        val clampedStart = start.coerceIn(0f, length)
        val clampedEnd = (start + segmentLength).coerceIn(0f, length)
        if (clampedEnd > clampedStart) {
            val segment = Path()
            pathMeasure.getSegment(clampedStart, clampedEnd, segment, true)
            drawPath(
                segment,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = coreStrokeWidth * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
