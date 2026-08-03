package com.example.animetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.animetracker.R
import kotlinx.coroutines.delay
import kotlin.math.sin

// ============================================================================
// ReiWordmarkDrawn
// ----------------------------------------------------------------------------
// Replaces the generic ReiSealWordmark badge with your actual "Rei 零" mark
// (res/drawable/logo_rei_wordmark.png — the pink cursive + kanji lockup),
// revealed with a left-to-right "ink" wipe so it looks hand-written as the
// splash loads, rather than just fading/popping in.
//
// How the reveal works:
//   1. The wordmark bitmap is drawn into an offscreen layer.
//   2. A horizontal gradient mask (opaque -> transparent, DstIn blend) is
//      drawn over it and animated left-to-right, uncovering the artwork
//      progressively instead of scaling/fading it as a whole.
//   3. A small warm glow rides the reveal edge, with a light vertical
//      wobble, to sell the "pen tip" — then fades once the mark is fully
//      drawn.
//
// This is a raster wipe, not true stroke-path tracing (that needs the mark
// as vector path data). For a splash shown for ~1-2s it reads convincingly
// as handwriting; swap in a real path-trim animation later if you vectorize
// the mark.
// ============================================================================

private val InkGlow = Color(0xFFFFE3EC)

@Composable
fun ReiWordmarkDrawn(
    modifier: Modifier = Modifier,
    width: Dp = 240.dp,
    startDelayMs: Int = 750,
    revealDurationMs: Int = 1500,
) {
    val painter = painterResource(id = R.drawable.logo_rei_wordmark)
    // Intrinsic aspect ratio of logo_rei_wordmark.png (1384 x 590 source crop)
    val aspect = 1384f / 590f

    val progress = remember { Animatable(0f) }
    val tipAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(startDelayMs.toLong())
        tipAlpha.snapTo(1f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = revealDurationMs,
                easing = CubicBezierEasing(0.35f, 0f, 0.25f, 1f)
            )
        )
        tipAlpha.animateTo(0f, tween(350))
    }

    Canvas(
        modifier = modifier
            .width(width)
            .aspectRatio(aspect)
    ) {
        val w = size.width
        val h = size.height
        val p = progress.value

        drawIntoCanvas { canvas ->
            val contentPaint = Paint()
            canvas.saveLayer(Rect(Offset.Zero, Size(w, h)), contentPaint)

            // 1. draw the wordmark artwork filling this box
            with(painter) { draw(size = Size(w, h)) }

            // 2. mask it with an animated left-to-right reveal (DstIn keeps
            //    destination pixels only where this mask is opaque)
            val maskPaint = Paint().apply { blendMode = BlendMode.DstIn }
            canvas.saveLayer(Rect(Offset.Zero, Size(w, h)), maskPaint)

            val edge = (p * 1.12f - 0.06f).coerceIn(0f, 1f) // slight overshoot so it fully clears
            val feather = 0.09f
            drawRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to Color.Black,
                        (edge - feather).coerceIn(0f, 1f) to Color.Black,
                        edge to Color.Transparent,
                        1f to Color.Transparent
                    ),
                    startX = 0f,
                    endX = w
                ),
                size = Size(w, h)
            )
            canvas.restore() // composite mask onto content layer
            canvas.restore() // composite content layer onto screen
        }

        // 3. traveling glow "pen tip" along the reveal edge, with a gentle
        //    wobble so it doesn't read as a mechanical scanline
        if (tipAlpha.value > 0f) {
            val tipX = (p * 1.12f - 0.06f).coerceIn(0f, 1f) * w
            val wobble = sin(p * 22f) * h * 0.05f
            val tipY = h * 0.52f + wobble
            val glowRadius = h * 0.22f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        InkGlow.copy(alpha = 0.55f * tipAlpha.value),
                        InkGlow.copy(alpha = 0f)
                    ),
                    center = Offset(tipX, tipY),
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = InkGlow.copy(alpha = 0.8f * tipAlpha.value),
                radius = h * 0.035f,
                center = Offset(tipX, tipY)
            )
        }
    }
}
