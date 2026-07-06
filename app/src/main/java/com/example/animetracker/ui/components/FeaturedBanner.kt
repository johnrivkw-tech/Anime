package com.example.animetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.animetracker.ui.model.HomeCardItem
import com.example.animetracker.ui.theme.Blaze
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.Pulse
import com.example.animetracker.ui.theme.VizoraLogoFont
import java.util.Locale

/**
 * The signature moment of the app: a full-bleed hero banner (Crunchyroll-style)
 * with the "Vizora" wordmark floating in cursive over the top-left of the
 * artwork, and the AI action sitting where a search icon would normally go
 * (top-right, over the image, not in a separate app bar).
 */
@Composable
fun FeaturedBanner(
    item: HomeCardItem?,
    onClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Charcoal)
        ) {
            CircularProgressIndicator(color = Blaze, modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShapeBottomOnly())
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.bannerUrl ?: item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Vertical scrim for legibility.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f)),
                        startY = 60f
                    )
                )
        )
        // Blaze-tinted wash from the bottom-left corner.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Blaze.copy(alpha = 0.35f), Color.Transparent),
                        radius = 420f
                    )
                )
        )
        // Dark scrim behind the top row so the wordmark and icon stay legible
        // even over bright artwork.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )

        // Top row: cursive "Vizora" wordmark (left) + AI action (right, where
        // a search icon normally sits).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vizora",
                fontFamily = VizoraLogoFont,
                fontSize = 32.sp,
                color = Bone
            )
            IconButton(
                onClick = onAiClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Recommendations",
                    tint = Pulse
                )
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(
                text = "FEATURED",
                style = MaterialTheme.typography.labelLarge,
                color = Pulse,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.displayLarge,
                color = Bone,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.score != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Pulse,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", item.score),
                        color = Bone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
