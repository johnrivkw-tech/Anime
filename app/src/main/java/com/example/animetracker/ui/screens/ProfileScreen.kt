package com.example.animetracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.ui.components.AnimeSectionRow
import com.example.animetracker.ui.model.GenreCount
import com.example.animetracker.ui.model.ProfileBadge
import com.example.animetracker.ui.model.ProfileStats
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit = {}) {
    val bannerPath by viewModel.profileBannerPath.collectAsState()
    val avatarPath by viewModel.profileAvatarPath.collectAsState()
    val isBannerSaving by viewModel.isBannerSaving.collectAsState()
    val isAvatarSaving by viewModel.isAvatarSaving.collectAsState()
    val displayName by viewModel.profileDisplayName.collectAsState()
    val stats by viewModel.profileStats.collectAsState()
    val favorites by viewModel.favoriteAnime.collectAsState()

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setProfileBanner(it) } }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setProfileAvatar(it) } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                ProfileHeader(
                    bannerPath = bannerPath,
                    avatarPath = avatarPath,
                    isBannerSaving = isBannerSaving,
                    isAvatarSaving = isAvatarSaving,
                    displayName = displayName,
                    joinedAtMillis = viewModel.profileJoinedAtMillis,
                    onPickBanner = {
                        bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onPickAvatar = {
                        avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onNameChanged = { viewModel.setDisplayName(it) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OverviewCard(stats = stats, modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Your Stats")
                Spacer(modifier = Modifier.height(8.dp))
                StatsGrid(stats = stats, modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Watch Time")
                Spacer(modifier = Modifier.height(8.dp))
                WatchTimeCard(
                    days = stats.watchDays,
                    hours = stats.watchHoursRemainder,
                    totalMinutes = stats.totalWatchMinutes,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (stats.topGenres.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionLabel("Top Genres")
                    Spacer(modifier = Modifier.height(8.dp))
                    TopGenresCard(
                        genres = stats.topGenres,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            if (favorites.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimeSectionRow(
                        title = "Favorites",
                        items = favorites,
                        isLoading = false,
                        onItemClick = { item -> item.aniListId?.let(onAnimeClick) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionLabel("Achievements")
                Spacer(modifier = Modifier.height(8.dp))
                AchievementsGrid(
                    badges = computeBadges(stats),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ProfileHeader(
    bannerPath: String?,
    avatarPath: String?,
    isBannerSaving: Boolean,
    isAvatarSaving: Boolean,
    displayName: String,
    joinedAtMillis: Long,
    onPickBanner: () -> Unit,
    onPickAvatar: () -> Unit,
    onNameChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            if (bannerPath != null) {
                AsyncImage(
                    model = File(bannerPath),
                    contentDescription = "Profile banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))))
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                IconButton(onClick = onPickBanner) {
                    if (isBannerSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Change banner photo", tint = Color.White)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (avatarPath != null) {
                        AsyncImage(
                            model = File(avatarPath),
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = displayName.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    IconButton(onClick = onPickAvatar) {
                        if (isAvatarSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White)
                        } else {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Change profile photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-28).dp)) {
            EditableDisplayName(displayName = displayName, onNameChanged = onNameChanged)
            Text(
                text = "Member since ${formatJoinedDate(joinedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditableDisplayName(displayName: String, onNameChanged: (String) -> Unit) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var draft by remember(displayName) { mutableStateOf(displayName) }

    if (isEditing) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                placeholder = { Text("Your name") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                onNameChanged(draft.trim())
                isEditing = false
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Save name", tint = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = displayName.ifBlank { "Add your name" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (displayName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Bone
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = { isEditing = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit name",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatJoinedDate(millis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.US).format(java.util.Date(millis))

/** Big card up top: completion ring on the left, headline numbers on the right. */
@Composable
private fun OverviewCard(stats: ProfileStats, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompletionRing(rate = stats.completionRate, modifier = Modifier.size(76.dp))
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = "${(stats.completionRate * 100).roundToInt()}% completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.completed} of ${stats.totalAnime} tracked titles finished",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stats.ratedCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f avg rating · %d rated", stats.averageRating, stats.ratedCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionRing(rate: Float, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.background
    val progressColor = MaterialTheme.colorScheme.primary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.14f
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * rate.coerceIn(0f, 1f),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        Text(
            text = "${(rate * 100).roundToInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsGrid(stats: ProfileStats, modifier: Modifier = Modifier) {
    val cards = listOf(
        Triple(Icons.Filled.List, "Total", stats.totalAnime.toString()),
        Triple(Icons.Filled.PlayCircle, "Watching", stats.watching.toString()),
        Triple(Icons.Filled.CheckCircle, "Completed", stats.completed.toString()),
        Triple(Icons.Filled.Schedule, "Plan to Watch", stats.planToWatch.toString()),
        Triple(Icons.Filled.Favorite, "Favorites", stats.favorites.toString()),
        Triple(Icons.Filled.AutoStories, "Manga", stats.mangaCount.toString()),
        Triple(Icons.Filled.MenuBook, "Light Novels", stats.lightNovelCount.toString()),
        Triple(Icons.Filled.Star, "Episodes Seen", stats.totalEpisodesWatched.toString())
    )
    Column(modifier = modifier) {
        cards.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (icon, label, value) ->
                    StatCard(icon = icon, label = label, value = value, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WatchTimeCard(days: Long, hours: Long, totalMinutes: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$days days, $hours hours",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Total time spent watching",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (totalMinutes > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = watchTimeFunFact(totalMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** A lighthearted equivalence for the raw minute count, e.g. "~14 movies (110 min each)". */
private fun watchTimeFunFact(totalMinutes: Long): String {
    val movieCount = max(1L, totalMinutes / 110)
    return "That's about $movieCount feature-length movies (110 min each)"
}

@Composable
private fun TopGenresCard(genres: List<GenreCount>, modifier: Modifier = Modifier) {
    val maxCount = genres.maxOf { it.count }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            genres.forEachIndexed { index, genreCount ->
                GenreBar(genreCount = genreCount, fraction = genreCount.count.toFloat() / maxCount.toFloat())
                if (index != genres.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun GenreBar(genreCount: GenreCount, fraction: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = genreCount.genre, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = genreCount.count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.04f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )
        }
    }
}

@Composable
private fun AchievementsGrid(badges: List<ProfileBadge>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        badges.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { badge ->
                    BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BadgeCard(badge: ProfileBadge, modifier: Modifier = Modifier) {
    val contentColor = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else Smoke
    Surface(
        modifier = modifier.aspectRatio(1.5f),
        shape = RoundedCornerShape(14.dp),
        color = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(imageVector = badge.icon, contentDescription = null, tint = contentColor)
                if (!badge.isUnlocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Smoke,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (badge.isUnlocked) Bone else Smoke,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

/**
 * Fun, purely-derived achievements. Nothing here is stored — every badge is
 * recomputed from the current [ProfileStats] on every recomposition, so
 * there's no risk of them ever drifting out of sync with the real library.
 */
private fun computeBadges(stats: ProfileStats): List<ProfileBadge> = listOf(
    ProfileBadge(
        title = "Getting Started",
        description = "Track your first title",
        icon = Icons.Filled.PlayCircle,
        isUnlocked = stats.totalAnime >= 1
    ),
    ProfileBadge(
        title = "Collector",
        description = "Track 25+ titles",
        icon = Icons.Filled.List,
        isUnlocked = stats.totalAnime >= 25
    ),
    ProfileBadge(
        title = "Completionist",
        description = "Finish 10+ titles",
        icon = Icons.Filled.CheckCircle,
        isUnlocked = stats.completed >= 10
    ),
    ProfileBadge(
        title = "Binge Master",
        description = "24+ hours watched",
        icon = Icons.Filled.LocalFireDepartment,
        isUnlocked = stats.totalWatchMinutes >= 24 * 60
    ),
    ProfileBadge(
        title = "Marathoner",
        description = "7+ full days watched",
        icon = Icons.Filled.EmojiEvents,
        isUnlocked = stats.watchDays >= 7
    ),
    ProfileBadge(
        title = "Critic",
        description = "Rate 10+ titles",
        icon = Icons.Filled.Star,
        isUnlocked = stats.ratedCount >= 10
    ),
    ProfileBadge(
        title = "Bookworm",
        description = "5+ manga & novels",
        icon = Icons.Filled.MenuBook,
        isUnlocked = (stats.mangaCount + stats.lightNovelCount) >= 5
    ),
    ProfileBadge(
        title = "Otaku Elite",
        description = "10+ favorites",
        icon = Icons.Filled.Favorite,
        isUnlocked = stats.favorites >= 10
    )
)
