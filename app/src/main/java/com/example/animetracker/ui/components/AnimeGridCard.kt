package com.example.animetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.animetracker.ui.model.HomeCardItem
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke

/**
 * Plain poster tile used on Search/Browse: box-shaped artwork with the
 * title and a small info line living below the image (not overlaid on
 * top of it), matching a typical streaming-app catalog grid.
 */
@Composable
fun AnimeGridCard(item: HomeCardItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
        )

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.Medium,
            color = Bone,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )

        val subtitle = item.genres.take(2).joinToString(" · ").ifBlank {
            item.statusLabel ?: item.score?.let { "★ %.1f".format(it) }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
