package com.example.animetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// ============================================================================
// ReiSealWordmark
// ----------------------------------------------------------------------------
// A replacement for ReiWordmark, built for the shrine splash (and reusable
// anywhere the app wants a more premium mark than a plain header logo).
// Where the old mark was a generic rotated-square "tech badge," this one is
// built from actual Japanese design language:
//
//   - A vermillion HANKO (personal seal/stamp) circle — the way a maker's
//     seal is stamped on calligraphy and woodblock prints — rendered with
//     a slightly irregular edge so it reads as stamped ink, not vector-perfect.
//   - A single abstract brush stroke inside it. This is deliberately NOT a
//     real kanji character (guessing at one risks getting the meaning
//     wrong) — it's an original calligraphic swipe, evoking motion/energy,
//     the same way the old mark's diagonal cut did, just rendered by hand
//     instead of as a straight ruled line.
//   - A thin gold ring around the seal, the kind of accent line used on
//     lacquerware and shrine ornamentation.
//   - "REI" set in a refined, wide-tracked serif weight with a warm
//     gold-to-crimson gradient, sitting below the seal rather than beside
//     it — a lockup, not a badge-plus-text pairing.
// ============================================================================

private val SealVermillion = Color(0xFFB3251A)
private val SealVermillionDeep = Color(0xFF7A1610)
private val SealGold = Color(0xFFE8B860)
private val SealInk = Color(0xFFFFF6E8)

/**
 * Rei's mark: a hand-stamped vermillion seal with a brush-stroke glyph,
 * a thin gold ring, and a serif gradient wordmark beneath it.
 *
 * @param markSize Diameter of the seal circle. Wordmark font size and ring
 *   thickness scale off this, so one size controls the whole lockup.
 */
@Composable
fun ReiSealWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 88.dp,
) {
    val fontSize: TextUnit = (markSize.value * 0.30f).sp

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(markSize)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = size.minDimension / 2f

            // Soft ambient glow behind the seal so it lifts off a busy
            // background instead of just sitting flat on top of it.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SealVermillion.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r * 1.9f
                ),
                radius = r * 1.9f,
                center = Offset(cx, cy)
            )

            // Hand-stamped seal disc: a circle path perturbed slightly so
            // the outline isn't perfectly round, like real stamped ink.
            val seal = Path().apply {
                val points = 48
                for (i in 0..points) {
                    val angle = (i.toFloat() / points) * 2f * PI.toFloat()
                    val wobble = 1f + 0.018f * sin(angle * 7f) + 0.012f * sin(angle * 13f + 1.3f)
                    val px = cx + cos(angle) * r * 0.94f * wobble
                    val py = cy + sin(angle) * r * 0.94f * wobble
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = seal,
                brush = Brush.radialGradient(
                    colors = listOf(SealVermillion, SealVermillionDeep),
                    center = Offset(cx, cy * 0.85f),
                    radius = r * 1.4f
                )
            )

            // Thin gold ring, inset slightly from the seal edge.
            drawCircle(
                color = SealGold.copy(alpha = 0.85f),
                radius = r * 0.94f,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.045f)
            )

            // Abstract brush-stroke glyph: a single tapered swipe with a
            // short cross-stroke, echoing calligraphy without claiming to
            // be a specific character.
            val stroke = Path().apply {
                moveTo(cx - r * 0.32f, cy - r * 0.42f)
                quadraticBezierTo(
                    cx + r * 0.05f, cy - r * 0.05f,
                    cx + r * 0.30f, cy + r * 0.44f
                )
            }
            drawPath(
                path = stroke,
                color = SealInk,
                style = Stroke(width = r * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val crossStroke = Path().apply {
                moveTo(cx - r * 0.34f, cy + r * 0.08f)
                lineTo(cx + r * 0.10f, cy - r * 0.14f)
            }
            drawPath(
                path = crossStroke,
                color = SealInk.copy(alpha = 0.92f),
                style = Stroke(width = r * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Spacer(modifier = Modifier.height(markSize * 0.14f))

        Text(
            text = "R E I",
            textAlign = TextAlign.Center,
            style = TextStyle(
                brush = Brush.horizontalGradient(listOf(SealGold, SealVermillion, SealGold)),
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = (fontSize.value * 0.18f).sp
            )
        )
    }
}
