package com.example.animetracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import coil.compose.AsyncImage
import com.example.animetracker.data.Anime
import com.example.animetracker.data.AnimeStatus
import com.example.animetracker.data.network.AniListCharacterEdge
import com.example.animetracker.data.network.AniListMedia
import com.example.animetracker.data.network.AniListRelationEdge
import com.example.animetracker.data.network.AniListStreamingEpisode
import com.example.animetracker.ui.model.NameGradient
import com.example.animetracker.ui.model.textStyle
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

/** Which of the three mutually-exclusive states the Details screen is in, driving the [Crossfade] below. */
private enum class DetailsUiState { Loading, Error, Content }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailsScreen(
    viewModel: AnimeViewModel,
    aniListId: Int,
    onBack: () -> Unit,
    onAnimeClick: (Int) -> Unit = {}
) {
    val details by viewModel.animeDetails.collectAsState()
    val isLoading by viewModel.isDetailsLoading.collectAsState()
    val error by viewModel.detailsError.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val allLocal by viewModel.allLocalAnime.collectAsState()
    val titleGradient by viewModel.titleGradient.collectAsState()
    val context = LocalContext.current

    val localEntry = remember(allLocal, aniListId) { allLocal.firstOrNull { it.aniListId == aniListId } }

    LaunchedEffect(aniListId) {
        viewModel.loadAnimeDetails(aniListId)
        viewModel.loadAnimeCharacters(aniListId)
    }

    // No TopAppBar here on purpose — back/tracking controls are overlaid
    // directly on the hero art instead of sitting in a solid bar, so the
    // banner reads edge-to-edge all the way to the top of the screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content/loading/error crossfade so switching between them (first
        // load finishing, or a retry landing) is a soft dissolve rather
        // than a hard cut. `details != null` is checked first so a
        // pull-to-refresh reload (which briefly flips isLoading back to
        // true) never flashes back to the loading state while old data is
        // still on screen.
        val uiState = when {
            details != null -> DetailsUiState.Content
            isLoading -> DetailsUiState.Loading
            error != null -> DetailsUiState.Error
            else -> DetailsUiState.Loading
        }
        Crossfade(targetState = uiState, label = "details-ui-state") { state ->
            when (state) {
                DetailsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading details…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Smoke
                            )
                        }
                    }
                }
                DetailsUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Something went wrong",
                            color = Smoke,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.loadAnimeDetails(aniListId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Bone)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DetailsUiState.Content -> {
                    DetailsContent(
                        details = details!!,
                        localEntry = localEntry,
                        characters = characters,
                        titleGradient = titleGradient,
                        isRefreshing = isLoading,
                        onRefresh = {
                            viewModel.loadAnimeDetails(aniListId)
                            viewModel.loadAnimeCharacters(aniListId)
                        },
                        onSetStatus = { status -> viewModel.setAnimeStatus(details!!, localEntry, status) },
                        onRemove = { entry -> viewModel.deleteAnime(entry) },
                        onMarkEpisodeWatched = { entry -> viewModel.incrementEpisode(entry) },
                        onSelectRelated = onAnimeClick,
                        onOpenEpisode = { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    )
                }
            }
        }

        // Permanent top vignette behind the floating back/tracking controls,
        // so they stay legible over bright banner art no matter how far
        // the content beneath has scrolled.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        // Fixed overlay: back button top-left, tracking status top-right —
        // both float on translucent circular scrims over whatever's
        // scrolling underneath instead of a solid app bar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.38f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
                }
            }
            if (details != null) {
                TrackingStatusButton(
                    localEntry = localEntry,
                    onSetStatus = { status -> viewModel.setAnimeStatus(details!!, localEntry, status) },
                    onRemove = { entry -> viewModel.deleteAnime(entry) }
                )
            }
        }
    }
}

/**
 * The heart icon doubles as the whole tracking control now: tap it to open
 * a dropdown with Watching/Completed/Plan to Watch (adding the title to the
 * list if it isn't tracked yet) plus a Remove option once it is. Replaces
 * the old always-visible status chip row + separate Remove button at the
 * bottom of the screen.
 */
@Composable
private fun TrackingStatusButton(
    localEntry: Anime?,
    onSetStatus: (AnimeStatus) -> Unit,
    onRemove: (Anime) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Box {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.38f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = if (localEntry != null) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Tracking status",
                    tint = if (localEntry != null) MaterialTheme.colorScheme.secondary else Bone
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AnimeStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    leadingIcon = {
                        if (localEntry?.status == status) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetStatus(status)
                        expanded = false
                    }
                )
            }
            if (localEntry != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Remove from List", color = ErrorRed) },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemove(localEntry)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Small rounded icon badge used to the left of every section title. */
@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Bone,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

/** Small icon + label pill used for the season/format/episode/duration facts. */
@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Bone, maxLines = 1)
        }
    }
}

/** Accent color used for the airing-status dot/pill. */
private fun statusAccentColor(status: String): Color = when (status) {
    "Currently Airing" -> Color(0xFF22C55E)
    "Not Yet Aired" -> Color(0xFFFFC107)
    "Cancelled" -> ErrorRed
    "On Hiatus" -> Color(0xFFFF9800)
    else -> Smoke
}

/** Dot + label pill summarizing airing status, color-coded by state. */
@Composable
private fun StatusPill(status: String) {
    val accent = statusAccentColor(status)
    Surface(
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.14f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = status, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
}

/** Compact "12.4K favorites" / "#4 popularity" style formatting for large counts. */
private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsContent(
    details: AniListMedia,
    localEntry: Anime?,
    characters: List<AniListCharacterEdge>,
    titleGradient: NameGradient,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSetStatus: (AnimeStatus) -> Unit,
    onRemove: (Anime) -> Unit,
    onMarkEpisodeWatched: (Anime) -> Unit,
    onSelectRelated: (Int) -> Unit,
    onOpenEpisode: (String) -> Unit
) {
    val scroll = rememberScrollState()
    val infoScroll = rememberScrollState()
    // The hero parallax/collapse reads the vertical scroll position every
    // frame; HeroSection reads `listState` directly inside its
    // graphicsLayer blocks (rather than this composable computing a Float
    // and passing it down), so scrolling drives the draw phase without
    // triggering a recomposition of the whole details list on every pixel.
    val listState = rememberLazyListState()
    val seasonsListState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                HeroSection(details = details, listState = listState)
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = details.displayTitle,
                        style = titleGradient.textStyle(
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Bone
                            )
                        )
                    )

                    val otherNames = details.synonyms.filter { it.isNotBlank() }.take(3)
                    if (otherNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Also known as: ${otherNames.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = Smoke,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val score = details.score
                        if (score != null) {
                            ScorePill(score = score)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        val statusLabel = details.status
                        if (statusLabel != null) {
                            StatusPill(status = statusLabel)
                        } else {
                            Text(
                                text = "Unknown status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Smoke
                            )
                        }
                    }

                    // Popularity/favourites, when AniList reports them —
                    // gives the score above some context beyond a bare
                    // number (e.g. an 8.9 backed by 400K favorites reads
                    // very differently than one backed by 400).
                    val popularity = details.popularity
                    val favourites = details.favourites
                    if (popularity != null || favourites != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (popularity != null) {
                                Text(
                                    text = "${formatCount(popularity)} on lists",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Smoke
                                )
                            }
                            if (favourites != null) {
                                Text(
                                    text = "${formatCount(favourites)} favorites",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Smoke
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val seasonYearLabel = listOfNotNull(
                        details.season?.lowercase()?.replaceFirstChar { it.uppercase() },
                        details.seasonYear?.toString()
                    ).joinToString(" ").ifBlank { null }
                    val episodeLabel = if (details.episodes != null) "${details.episodes} eps" else "Eps unknown"
                    // AniList's per-episode runtime in minutes, shown only when
                    // the details query reported one (most TV anime do).
                    val durationLabel = details.duration?.let { "~$it min" }
                    Row(
                        modifier = Modifier.horizontalScroll(infoScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (seasonYearLabel != null) {
                            InfoChip(icon = Icons.Filled.DateRange, label = seasonYearLabel)
                        }
                        details.formatLabel?.let { InfoChip(icon = Icons.Filled.Videocam, label = it) }
                        InfoChip(icon = Icons.Filled.PlayArrow, label = episodeLabel)
                        if (durationLabel != null) {
                            InfoChip(icon = Icons.Filled.AccessTime, label = durationLabel)
                        }
                    }

                    if (details.studioNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = Smoke,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = details.studioNames.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Smoke,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (details.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(scroll),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            details.genres.forEachIndexed { index, genreName ->
                                val accent = if (index % 2 == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = accent.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = genreName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (details.streamingEpisodes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(28.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(20.dp))

                        val episodeChunks = remember(details.id, details.streamingEpisodes) {
                            buildEpisodeChunks(details.streamingEpisodes)
                        }
                        var selectedChunk by remember(details.id) { mutableStateOf(0) }
                        val safeSelectedChunk = if (episodeChunks.isEmpty()) 0 else selectedChunk.coerceIn(0, episodeChunks.lastIndex)
                        var episodesExpanded by remember(details.id) { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { episodesExpanded = !episodesExpanded }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                color = Bone,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${details.streamingEpisodes.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (episodesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (episodesExpanded) "Collapse episodes" else "Expand episodes",
                                tint = Smoke
                            )
                        }

                        AnimatedVisibility(
                            visible = episodesExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                // Only bother with a range picker once there's more than
                                // one page — a 12-episode show doesn't need it.
                                if (episodeChunks.size > 1) {
                                    EpisodeRangeDropdown(
                                        chunks = episodeChunks,
                                        selectedIndex = safeSelectedChunk,
                                        onSelect = { selectedChunk = it }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    episodeChunks.getOrNull(safeSelectedChunk)?.episodes?.forEach { episode ->
                                        EpisodeListRow(episode = episode, onClick = { episode.url?.let(onOpenEpisode) })
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(title = "Synopsis", icon = Icons.Filled.Description)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Synopsis: clamped to a few lines with a More/Less toggle so
                    // long write-ups don't swallow the screen (state is per-anime).
                    val synopsis = details.synopsis ?: "No synopsis available."
                    val synopsisLong = synopsis.length > 160
                    var synopsisExpanded by remember(details.id) { mutableStateOf(false) }
                    Text(
                        text = synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Smoke,
                        maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
                        overflow = if (synopsisExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                    if (synopsisLong) {
                        TextButton(
                            onClick = { synopsisExpanded = !synopsisExpanded },
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (synopsisExpanded) "Less" else "More",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (details.seasonsAndArcs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        title = "Seasons & Arcs",
                        icon = Icons.Filled.Collections,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Snap fling so a flick settles on a card boundary
                    // instead of coasting to an arbitrary stopping point —
                    // reads as "swiping between" seasons rather than
                    // freely scrolling a list.
                    LazyRow(
                        state = seasonsListState,
                        flingBehavior = rememberSnapFlingBehavior(seasonsListState),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(details.seasonsAndArcs, key = { it.node.id }) { edge ->
                            RelatedSeasonCard(edge = edge, onClick = { onSelectRelated(edge.node.id) })
                        }
                    }
                }
            }
            if (localEntry != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(title = "Your List", icon = Icons.Filled.Bookmark)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                item {
                    TrackingSection(
                        entry = localEntry,
                        onMarkEpisodeWatched = onMarkEpisodeWatched
                    )
                }
            }
            if (characters.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        title = "Characters",
                        icon = Icons.Filled.People,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hold a character to see their bio",
                        style = MaterialTheme.typography.labelSmall,
                        color = Smoke,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(characters.take(15), key = { it.node.id }) { entry ->
                            CharacterCard(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

// Hero motion tuning — factors of the hero's scroll offset (in px).
private const val HERO_PARALLAX_FACTOR = 0.3f
private const val HERO_POSTER_RIDE_FACTOR = 0.5f
private const val HERO_POSTER_SHRINK = 0.06f
private val HERO_HEIGHT: Dp = 300.dp

@Composable
private fun HeroSection(details: AniListMedia, listState: LazyListState) {
    Box(modifier = Modifier.fillMaxWidth().height(HERO_HEIGHT)) {
        // Parallax backdrop: moves down at a fraction of the scroll speed,
        // so the art lingers and reveals as you scroll instead of scrolling
        // away flat. Clipped to the hero so it never paints over the
        // content below.
        //
        // The scroll offset is read directly from `listState` inside each
        // graphicsLayer block below (draw-phase state reads), not computed
        // once up in DetailsContent and passed down as a plain Float — that
        // would make DetailsContent (and everything in this LazyColumn
        // item) recompose on every pixel of scroll. Reading it here instead
        // means only the draw phase re-runs per frame; HeroSection itself
        // never recomposes from scrolling.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds()
                .graphicsLayer {
                    val heroOffsetPx = if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        HERO_HEIGHT.toPx()
                    }
                    translationY = heroOffsetPx.coerceAtMost(HERO_HEIGHT.toPx()) * HERO_PARALLAX_FACTOR
                }
        ) {
            if (details.bannerImage != null) {
                AsyncImage(
                    model = details.bannerImage,
                    contentDescription = details.displayTitle,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF1A1820)),
                    error = ColorPainter(Color(0xFF1A1820)),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // No banner art — show the poster blurred, scaled up, and
                // darkened behind a scrim instead of cropping it into a
                // landscape strip.
                AsyncImage(
                    model = details.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF1A1820)),
                    error = ColorPainter(Color(0xFF1A1820)),
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.2f)
                        .blur(32.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }
            // Subtle darkening across the whole banner so art of any
            // brightness still reads as a backdrop rather than the focal
            // point, plus the fade into the background color below.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                        )
                    )
            )
        }
        // Framed poster: rides up and shrinks slightly as the hero scrolls
        // away, tucking in toward the content below instead of scrolling
        // flat. Shrinks from its bottom edge so it collapses into the list.
        // A soft gradient rim (primary → secondary) gives it a premium,
        // deliberately-designed feel instead of a flat single-color border.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp)
                .offset(y = 40.dp)
                .size(width = 110.dp, height = 156.dp)
                .graphicsLayer {
                    val heroOffsetPx = if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        HERO_HEIGHT.toPx()
                    }
                    val clamped = heroOffsetPx.coerceAtMost(HERO_HEIGHT.toPx())
                    translationY = -clamped * HERO_POSTER_RIDE_FACTOR
                    val t = (clamped / HERO_HEIGHT.toPx()).coerceIn(0f, 1f)
                    val s = 1f - HERO_POSTER_SHRINK * t
                    scaleX = s
                    scaleY = s
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 16.dp,
            border = BorderStroke(
                2.dp,
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
        ) {
            AsyncImage(
                model = details.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color(0xFF1A1820)),
                error = ColorPainter(Color(0xFF1A1820)),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    Spacer(modifier = Modifier.height(44.dp))
}

/** Accent-tinted pill for the AniList aggregate score, e.g. "★ 8.45". */
@Composable
private fun ScorePill(score: Double) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = String.format("%.2f", score),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun TrackingSection(
    entry: Anime,
    onMarkEpisodeWatched: (Anime) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val totalKnown = entry.totalEpisodes > 0
            val progressText = if (totalKnown) {
                "Episode ${entry.episodesWatched} / ${entry.totalEpisodes}"
            } else {
                "Episode ${entry.episodesWatched}"
            }
            Text(text = progressText, style = MaterialTheme.typography.titleSmall, color = Bone)

            // A progress bar only makes sense when the total episode count is
            // known; otherwise the section stays exactly as before (text only).
            if (totalKnown) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (entry.episodesWatched.toFloat() / entry.totalEpisodes).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMarkEpisodeWatched(entry)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Bone)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mark Episode Watched", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** One 100-episode page of a show's streaming episode list, e.g. episodes 101-200. */
private data class EpisodeChunk(val range: IntRange, val episodes: List<AniListStreamingEpisode>)

/**
 * Splits a show's streaming episodes into pages of 100 by parsed episode
 * number (falling back to list position for entries AniList didn't give a
 * parseable number for), so a 1000+ episode show like One Piece gets a
 * "1-100 / 101-200 / ..." picker instead of one enormous unscrollable row.
 * Pages with no actual episodes in them (common — AniList's streaming data
 * for long-running shows is rarely gapless) are dropped rather than shown
 * empty.
 */
/**
 * Safety ceiling for how many pages we'll ever generate. AniList's episode
 * titles are free text, and [AniListStreamingEpisode.episodeNumber] parses
 * whatever number follows "Episode " with no upper bound — a single
 * mislabeled/garbage title (e.g. "Episode 2145 - ..." on a 20-episode show)
 * can otherwise inflate [maxNumber] enough that generating one chunk per
 * 100 episodes tries to allocate thousands of chunk objects and crashes
 * the app (OOM/ANR). If a show's parsed numbering would blow past this, we
 * don't trust it and fall back to one unpaginated list instead.
 */
private const val MAX_EPISODE_CHUNKS = 100

private fun buildEpisodeChunks(episodes: List<AniListStreamingEpisode>): List<EpisodeChunk> {
    val numbered = episodes.mapIndexed { index, ep -> (ep.episodeNumber ?: (index + 1)) to ep }
    val maxNumber = numbered.maxOfOrNull { it.first } ?: 0
    if (maxNumber <= 0) return listOf(EpisodeChunk(1..episodes.size, episodes))

    val chunkSize = 100
    val chunkCount = ((maxNumber - 1) / chunkSize) + 1
    if (chunkCount > MAX_EPISODE_CHUNKS) {
        // Parsed numbering doesn't look trustworthy (way more pages than any
        // real show would need) — skip pagination rather than risk it.
        return listOf(EpisodeChunk(1..episodes.size, episodes))
    }
    val chunks = (0 until chunkCount).map { i ->
        val range = (i * chunkSize + 1)..minOf((i + 1) * chunkSize, maxNumber)
        EpisodeChunk(range, numbered.filter { it.first in range }.map { it.second })
    }.filter { it.episodes.isNotEmpty() }
    // Defensive: every branch above should already guarantee at least one
    // non-empty chunk when `episodes` isn't empty, but never hand back an
    // empty list — callers index into this with coerceIn(0, lastIndex),
    // which throws on an empty range.
    return chunks.ifEmpty { listOf(EpisodeChunk(1..episodes.size, episodes)) }
}

@Composable
private fun EpisodeRangeDropdown(
    chunks: List<EpisodeChunk>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .clickable(
                    onClickLabel = "Change episode range, currently episodes " +
                        "${chunks[selectedIndex].range.first} to ${chunks[selectedIndex].range.last}"
                ) { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Ep ${chunks[selectedIndex].range.first}-${chunks[selectedIndex].range.last}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Bone
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Bone, modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            chunks.forEachIndexed { index, chunk ->
                DropdownMenuItem(
                    text = { Text("Episodes ${chunk.range.first}-${chunk.range.last}") },
                    leadingIcon = {
                        if (index == selectedIndex) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EpisodeListRow(episode: AniListStreamingEpisode, onClick: () -> Unit) {
    // Wrapped in a subtle rounded card so each episode reads as a tappable
    // item instead of a floating thumbnail + text pair.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.width(140.dp).height(90.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = episode.thumbnail,
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Bone,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val label = buildString {
                    if (episode.episodeNumber != null) append("${episode.episodeNumber}. ")
                    append(episode.cleanedTitle ?: episode.title ?: "Episode")
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Bone,
                    maxLines = 3
                )
                if (!episode.site.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = episode.site,
                        style = MaterialTheme.typography.labelSmall,
                        color = Smoke,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedSeasonCard(edge: AniListRelationEdge, onClick: () -> Unit) {
    val node = edge.node
    Column(
        modifier = Modifier
            .width(126.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(168.dp),
            shadowElevation = 6.dp
        ) {
            Box {
                AsyncImage(
                    model = node.posterUrl,
                    contentDescription = node.displayTitle,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxSize()
                )
                // The relation label ("Sequel", "Prequel"…) reads as a badge
                // laid directly over the art instead of plain text below it.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = edge.relationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Bone,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = node.displayTitle,
            style = MaterialTheme.typography.labelMedium,
            color = Bone,
            maxLines = 2
        )
        val meta = listOfNotNull(
            node.seasonYear?.toString(),
            node.episodes?.let { "$it ep" }
        ).joinToString(" • ")
        if (meta.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CharacterCard(entry: AniListCharacterEdge) {
    var showBio by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .width(84.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showBio = true
                },
                onLongClickLabel = "View bio"
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = entry.imageUrl,
                contentDescription = entry.displayName,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = Bone,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        // The character's role in the show ("Main", "Supporting"…), which
        // AniList already returns, shown as a small colored pill.
        entry.role?.let { role ->
            val label = role.lowercase().replaceFirstChar { it.uppercase() }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }
    }

    if (showBio) {
        CharacterBioSheet(entry = entry, onDismiss = { showBio = false })
    }
}

/** Bottom sheet opened by long-pressing a character card — portrait, role, and full bio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterBioSheet(entry: AniListCharacterEdge, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(width = 72.dp, height = 100.dp),
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = entry.imageUrl,
                        contentDescription = entry.displayName,
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Bone
                    )
                    entry.role?.let { role ->
                        val label = role.lowercase().replaceFirstChar { it.uppercase() }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = entry.node.bio ?: "No bio available for this character.",
                style = MaterialTheme.typography.bodyMedium,
                color = Smoke
            )
        }
    }
}
