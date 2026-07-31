package com.example.animetracker.ui.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Rei's wordmark: a small angular diamond badge with a bold brush-stroke
 * cut through it, next to bold, gradient-filled, letter-spaced text.
 * [fontSize]/[markSize] scale together so the same mark can be dropped
 * into both a compact header and a larger splash screen.
 */
@Composable
fun ReiWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    markSize: Dp = 28.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(markSize)) {
            val w = size.width
            val h = size.height

            // Rotated-square "diamond" badge, gradient-filled corner to corner.
            val diamond = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }
            drawPath(
                path = diamond,
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // A single bold diagonal stroke cut into the badge.
            val stroke = Path().apply {
                moveTo(w * 0.30f, h * 0.68f)
                lineTo(w * 0.70f, h * 0.30f)
            }
            drawPath(
                path = stroke,
                color = backgroundColor,
                style = Stroke(width = w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Spacer(modifier = Modifier.width(markSize * 0.3f))

        Text(
            text = "REI",
            style = TextStyle(
                brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)),
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = (fontSize.value * 0.05f).sp
            )
        )
    }
}
