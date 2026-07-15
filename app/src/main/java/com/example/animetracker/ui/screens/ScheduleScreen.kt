package com.example.animetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.data.network.AniListAiringSchedule
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Airing schedule tab: a horizontal day picker (a week centered on today)
 * over a list of every episode airing that day, oldest first, each with a
 * one-tap "+ My List" add — mirrors the day-picker + list layout used by
 * most anime-streaming apps' schedule screens.
 */
@Composable
fun ScheduleScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (Int) -> Unit,
    onSearchClick: () -> Unit
) {
    val selectedDate by viewModel.scheduleDate.collectAsState()
    val entries by viewModel.scheduleEntries.collectAsState()
    val isLoading by viewModel.isScheduleLoading.collectAsState()
    val error by viewModel.scheduleError.collectAsState()
    val localByAniListId by viewModel.localByAniListId.collectAsState()

    // 2 days back through 4 days ahead, centered on today.
    val days = remember { (-2..4).map { LocalDate.now().plusDays(it.toLong()) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Bone
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Bone)
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { date ->
                DayPill(
                    date = date,
                    isSelected = date == selectedDate,
                    onClick = { viewModel.selectScheduleDate(date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "", color = ErrorRed, modifier = Modifier.padding(24.dp))
            }
            entries.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Nothing airing this day.", color = Smoke)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(entries, key = { it.media.id.toString() + it.airingAt }) { entry ->
                    ScheduleRow(
                        entry = entry,
                        isInList = localByAniListId.containsKey(entry.media.id),
                        onClick = { onAnimeClick(entry.media.id) },
                        onAddClick = { viewModel.addAnimeFromSearchResult(entry.media) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPill(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.background else Smoke
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.background else Bone
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    entry: AniListAiringSchedule,
    isInList: Boolean,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val time = remember(entry.airingAt) {
        Instant.ofEpochSecond(entry.airingAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            color = Smoke,
            modifier = Modifier.width(48.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        AsyncImage(
            model = entry.media.posterUrl,
            contentDescription = entry.media.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = entry.media.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Bone,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onClick)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Episode ${entry.episode}",
                style = MaterialTheme.typography.bodySmall,
                color = Smoke
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = !isInList, onClick = onAddClick),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isInList) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (isInList) Smoke else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInList) "In My List" else "My List",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isInList) Smoke else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
