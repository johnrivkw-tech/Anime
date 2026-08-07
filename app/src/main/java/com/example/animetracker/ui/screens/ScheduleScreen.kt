package com.example.animetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.animetracker.data.network.AniListAiringSchedule
import com.example.animetracker.ui.components.FallbackNotice
import com.example.animetracker.ui.model.textStyle
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
 * over a timeline list of every episode airing that day, oldest first,
 * each with a one-tap "+ My List" add — mirrors the day-picker + list
 * layout used by most anime-streaming apps' schedule screens.
 *
 * Visual layer (no behavior change): the day pills are now richer cards
 * with a "TODAY" tag and an animated gradient selection, the list is drawn
 * as a timeline (time + dot + rail) with the next upcoming episode tagged
 * "Up next", and the empty state matches the rest of the app.
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
    val usingFallback by viewModel.scheduleUsingFallback.collectAsState()
    val titleGradient by viewModel.titleGradient.collectAsState()

    // 2 days back through 4 days ahead, centered on today.
    val days = remember { (-2..4).map { LocalDate.now().plusDays(it.toLong()) } }

    // The first episode that hasn't aired yet gets the "Up next" tag.
    val upNextKey = remember(entries) {
        val now = Instant.now().epochSecond
        entries.firstOrNull { it.airingAt >= now }
            ?.let { it.media.id.toString() + it.airingAt }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header — gradient title on the left with a relative date subtitle,
        // search icon on the right (matches the My List header layout).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schedule",
                    style = titleGradient.textStyle(
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Bone
                        )
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = scheduleSubtitle(selectedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Smoke
                )
            }
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
                ScheduleDayCard(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    onClick = { viewModel.selectScheduleDate(date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (usingFallback) {
            FallbackNotice(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "", color = ErrorRed, modifier = Modifier.padding(24.dp))
            }
            entries.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nothing airing this day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Bone
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pick another day to see what's on",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Smoke
                    )
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(entries, key = { it.media.id.toString() + it.airingAt }) { entry ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(350)) +
                            slideInVertically(
                                animationSpec = tween(350),
                                initialOffsetY = { it / 6 }
                            )
                    ) {
                        ScheduleTimelineRow(
                            entry = entry,
                            isInList = localByAniListId.containsKey(entry.media.id),
                            isUpNext = (entry.media.id.toString() + entry.airingAt) == upNextKey,
                            onClick = { onAnimeClick(entry.media.id) },
                            onAddClick = { viewModel.addAnimeFromSearchResult(entry.media) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Today · Wed, Aug 7"-style label for the header subtitle, with relative
 * day names for yesterday/today/tomorrow.
 */
private fun scheduleSubtitle(date: LocalDate): String {
    val dateText = date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))
    val today = LocalDate.now()
    val relative = when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> null
    }
    return if (relative != null) "$relative · $dateText" else dateText
}

@Composable
private fun ScheduleDayCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    // Two animated endpoints make the selected card crossfade from a flat
    // pill into a primary→dim gradient fill.
    val gradientStart by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else unselectedColor,
        animationSpec = tween(220),
        label = "dayGradientStart"
    )
    val gradientEnd by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else unselectedColor,
        animationSpec = tween(220),
        label = "dayGradientEnd"
    )
    val brush = remember(gradientStart, gradientEnd) {
        Brush.linearGradient(listOf(gradientStart, gradientEnd))
    }

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 10.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .clickable(onClick = onClick)
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.background else Bone
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (isToday) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.20f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            } else {
                // Reserve the same height as the TODAY tag so every pill in
                // the row stays the same size.
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ScheduleTimelineRow(
    entry: AniListAiringSchedule,
    isInList: Boolean,
    isUpNext: Boolean,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val time = remember(entry.airingAt) {
        Instant.ofEpochSecond(entry.airingAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline rail: time label, dot, then a vertical line filling the
        // rest of the row's height. The line gets its height from the card
        // beside it via IntrinsicSize.
        Column(
            modifier = Modifier
                .width(52.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium,
                color = if (isUpNext) MaterialTheme.colorScheme.primary else Smoke
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUpNext) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp)
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        ScheduleCard(
            entry = entry,
            isInList = isInList,
            isUpNext = isUpNext,
            onClick = onClick,
            onAddClick = onAddClick
        )
    }
}

@Composable
private fun ScheduleCard(
    entry: AniListAiringSchedule,
    isInList: Boolean,
    isUpNext: Boolean,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = entry.media.posterUrl,
                contentDescription = entry.media.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.media.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Bone,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.media.format?.let { format ->
                        MetaChip(text = formatLabel(format), color = MaterialTheme.colorScheme.secondary)
                    }
                    MetaChip(
                        text = if (entry.episode > 0) "EP ${entry.episode}" else "New episode",
                        color = Smoke
                    )
                    if (isUpNext) {
                        MetaChip(
                            text = "Up next",
                            color = MaterialTheme.colorScheme.primary,
                            filled = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // One-tap "My List" add — same behavior as before, restyled
                // as a pill that sits visibly on top of the card surface.
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isInList, onClick = onAddClick),
                    color = MaterialTheme.colorScheme.surfaceVariant,
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
}

/** Small rounded tag used for the format / episode / "Up next" labels. */
@Composable
private fun MetaChip(text: String, color: Color, filled: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (filled) MaterialTheme.colorScheme.background else color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color else color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** AniList's raw format strings ("TV_SHORT", "ONA"…) → friendly labels. */
private fun formatLabel(format: String?): String = when (format?.uppercase()) {
    "TV" -> "TV"
    "TV_SHORT" -> "Short"
    "MOVIE" -> "Movie"
    "ONA" -> "ONA"
    "OVA" -> "OVA"
    "SPECIAL" -> "Special"
    "MUSIC" -> "Music"
    else -> format ?: "TV"
}
