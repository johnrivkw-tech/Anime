package com.example.animetracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.ui.components.AnimeSectionRow
import com.example.animetracker.ui.model.Faction
import com.example.animetracker.ui.model.FavoriteAnimePick
import com.example.animetracker.ui.model.FavoriteCharacterPick
import com.example.animetracker.ui.model.GenreCount
import com.example.animetracker.ui.model.MAX_FAVORITE_PICKS
import com.example.animetracker.ui.model.ProfileStats
import com.example.animetracker.ui.model.RankTier
import com.example.animetracker.ui.model.currentRank
import com.example.animetracker.ui.model.nextRank
import com.example.animetracker.ui.model.ranksFor
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
fun ProfileScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit = {}, onBack: () -> Unit = {}) {
    val bannerPath by viewModel.profileBannerPath.collectAsState()
    val avatarPath by viewModel.profileAvatarPath.collectAsState()
    val isBannerSaving by viewModel.isBannerSaving.collectAsState()
    val isAvatarSaving by viewModel.isAvatarSaving.collectAsState()
    val displayName by viewModel.profileDisplayName.collectAsState()
    val stats by viewModel.profileStats.collectAsState()
    val favorites by viewModel.favoriteAnime.collectAsState()
    val faction by viewModel.faction.collectAsState()

    val favoriteAnimePicks by viewModel.favoriteAnimePicks.collectAsState()
    val favoriteCharacterPicks by viewModel.favoriteCharacterPicks.collectAsState()

    val animeSearchQuery by viewModel.onlineSearchQuery.collectAsState()
    val animeSearchResults by viewModel.searchResults.collectAsState()
    val isSearchingAnime by viewModel.isSearchingApi.collectAsState()
    val animeSearchError by viewModel.searchApiError.collectAsState()

    val characterSearchQuery by viewModel.characterSearchQuery.collectAsState()
    val characterSearchResults by viewModel.characterSearchResults.collectAsState()
    val isSearchingCharacters by viewModel.isSearchingCharacters.collectAsState()
    val characterSearchError by viewModel.characterSearchError.collectAsState()

    var showAnimePicker by rememberSaveable { mutableStateOf(false) }
    var showCharacterPicker by rememberSaveable { mutableStateOf(false) }

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setProfileBanner(it) } }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setProfileAvatar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                    rank = currentRank(faction, stats.completed),
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
                Spacer(modifier = Modifier.height(16.dp))
                WorthCard(berries = stats.berries, modifier = Modifier.padding(horizontal = 16.dp))
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
                SectionLabel("Rank")
                Spacer(modifier = Modifier.height(8.dp))
                RankSection(
                    faction = faction,
                    completed = stats.completed,
                    onFactionChange = { viewModel.setFaction(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionLabel("My Favorites")
                Spacer(modifier = Modifier.height(12.dp))
                FavoriteAnimeShelf(
                    picks = favoriteAnimePicks,
                    onAddClick = { showAnimePicker = true },
                    onRemove = { viewModel.removeFavoriteAnimePick(it) },
                    onItemClick = onAnimeClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                FavoriteCharacterShelf(
                    picks = favoriteCharacterPicks,
                    onAddClick = { showCharacterPicker = true },
                    onRemove = { viewModel.removeFavoriteCharacterPick(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showAnimePicker) {
        FavoriteAnimePickerDialog(
            query = animeSearchQuery,
            onQueryChange = { viewModel.searchOnline(it) },
            results = animeSearchResults,
            isLoading = isSearchingAnime,
            error = animeSearchError,
            alreadyPickedIds = favoriteAnimePicks.map { it.aniListId }.toSet(),
            onDismiss = {
                showAnimePicker = false
                viewModel.clearSearchResults()
            },
            onSelect = { media ->
                viewModel.addFavoriteAnimePick(media)
                if (viewModel.isFavoriteAnimeShelfFull()) {
                    showAnimePicker = false
                    viewModel.clearSearchResults()
                }
            }
        )
    }

    if (showCharacterPicker) {
        FavoriteCharacterPickerDialog(
            query = characterSearchQuery,
            onQueryChange = { viewModel.searchCharactersOnline(it) },
            results = characterSearchResults,
            isLoading = isSearchingCharacters,
            error = characterSearchError,
            alreadyPickedIds = favoriteCharacterPicks.map { it.characterId }.toSet(),
            onDismiss = {
                showCharacterPicker = false
                viewModel.clearCharacterSearchResults()
            },
            onSelect = { character ->
                viewModel.addFavoriteCharacterPick(character)
                if (viewModel.isFavoriteCharacterShelfFull()) {
                    showCharacterPicker = false
                    viewModel.clearCharacterSearchResults()
                }
            }
        )
    }
}

/**
 * Curated "Favorite Anime" shelf (max [MAX_FAVORITE_PICKS]) at the bottom of
 * the Profile screen. Distinct from the "Favorites" row further up, which
 * just reflects [com.example.animetracker.data.Anime.isFavorite] on tracked
 * titles — this one is a deliberate top-10 list of any AniList title.
 */
@Composable
private fun FavoriteAnimeShelf(
    picks: List<FavoriteAnimePick>,
    onAddClick: () -> Unit,
    onRemove: (Int) -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Favorite Anime (${picks.size}/$MAX_FAVORITE_PICKS)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(picks, key = { it.aniListId }) { pick ->
                FavoritePosterTile(
                    imageUrl = pick.imageUrl,
                    label = pick.title,
                    onClick = { onItemClick(pick.aniListId) },
                    onRemove = { onRemove(pick.aniListId) }
                )
            }
            if (picks.size < MAX_FAVORITE_PICKS) {
                item(key = "add_anime") {
                    AddTile(label = "Add anime", onClick = onAddClick)
                }
            }
        }
    }
}

/** Curated "Favorite Characters" shelf (max [MAX_FAVORITE_PICKS]), same idea as [FavoriteAnimeShelf]. */
@Composable
private fun FavoriteCharacterShelf(
    picks: List<FavoriteCharacterPick>,
    onAddClick: () -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Favorite Characters (${picks.size}/$MAX_FAVORITE_PICKS)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(picks, key = { it.characterId }) { pick ->
                FavoriteCircleTile(
                    imageUrl = pick.imageUrl,
                    label = pick.name,
                    onRemove = { onRemove(pick.characterId) }
                )
            }
            if (picks.size < MAX_FAVORITE_PICKS) {
                item(key = "add_character") {
                    AddTile(label = "Add character", shape = CircleShape, onClick = onAddClick)
                }
            }
        }
    }
}

@Composable
private fun FavoritePosterTile(
    imageUrl: String?,
    label: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.width(96.dp)) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 96.dp, height = 136.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick)
            )
            RemoveBadge(onRemove = onRemove, modifier = Modifier.align(Alignment.TopEnd))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FavoriteCircleTile(
    imageUrl: String?,
    label: String,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
            RemoveBadge(onRemove = onRemove, modifier = Modifier.align(Alignment.TopEnd))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun RemoveBadge(onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .size(22.dp)
            .clip(CircleShape),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        IconButton(onClick = onRemove, modifier = Modifier.size(22.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove from favorites",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddTile(
    label: String,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(10.dp)
) {
    val size = if (shape == CircleShape) 72.dp else 96.dp
    Column(
        modifier = Modifier.width(if (shape == CircleShape) 84.dp else 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = if (shape == CircleShape) size else 96.dp, height = if (shape == CircleShape) size else 136.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    rank: RankTier?,
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
            EditableDisplayName(displayName = displayName, rank = rank, onNameChanged = onNameChanged)
            Text(
                text = "Member since ${formatJoinedDate(joinedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditableDisplayName(displayName: String, rank: RankTier?, onNameChanged: (String) -> Unit) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = displayName.ifBlank { "Add your name" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (displayName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Bone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
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
            if (rank != null) {
                Spacer(modifier = Modifier.weight(1f))
                RankBadge(rank = rank)
            }
        }
    }
}

private fun formatJoinedDate(millis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.US).format(java.util.Date(millis))

/** A distinct icon + gradient for each of the 12 rank levels, shared by both factions. */
private data class RankVisual(val icon: ImageVector, val colors: List<Color>)

private fun rankVisualFor(level: Int): RankVisual = when (level) {
    1 -> RankVisual(Icons.Filled.Person, listOf(Color(0xFF6B7280), Color(0xFF9CA3AF)))
    2 -> RankVisual(Icons.Filled.DirectionsBoat, listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))
    3 -> RankVisual(Icons.Filled.Anchor, listOf(Color(0xFF0D9488), Color(0xFF2DD4BF)))
    4 -> RankVisual(Icons.Filled.Shield, listOf(Color(0xFF16A34A), Color(0xFF4ADE80)))
    5 -> RankVisual(Icons.Filled.Star, listOf(Color(0xFF0891B2), Color(0xFF22D3EE)))
    6 -> RankVisual(Icons.Filled.Bolt, listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)))
    7 -> RankVisual(Icons.Filled.MilitaryTech, listOf(Color(0xFFEA580C), Color(0xFFFB923C)))
    8 -> RankVisual(Icons.Filled.LocalFireDepartment, listOf(Color(0xFFDC2626), Color(0xFFF87171)))
    9 -> RankVisual(Icons.Filled.Whatshot, listOf(Color(0xFFBE123C), Color(0xFFFB7185)))
    10 -> RankVisual(Icons.Filled.WorkspacePremium, listOf(Color(0xFFCA8A04), Color(0xFFFACC15)))
    11 -> RankVisual(Icons.Filled.EmojiEvents, listOf(Color(0xFFD97706), Color(0xFFFCD34D)))
    else -> RankVisual(Icons.Filled.Diamond, listOf(Color(0xFFFF5A1F), Color(0xFF8B5CF6), Color(0xFFFF2D6B)))
}

/** Pill badge showing the current rank's icon and title, pinned to the right side of the name row. */
@Composable
private fun RankBadge(rank: RankTier, modifier: Modifier = Modifier) {
    val visual = remember(rank.level) { rankVisualFor(rank.level) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(visual.colors))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = rank.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Berries "net worth" card. Right now it's a straight readout of the same
 * berries total earned from watching (50/episode, 500/completed) — the
 * plan is to layer a real worth formula on top of this later, so this
 * card and the number it shows are deliberately kept separate from
 * [OverviewCard].
 */
@Composable
private fun WorthCard(berries: Long, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "${formatBerries(berries)} Berries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your worth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBerries(amount: Long): String = String.format(Locale.US, "%,d", amount)

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

/**
 * One Piece-flavored rank ladder. The user picks a faction (Pirate or
 * Marine) and climbs it purely by how many titles they've marked
 * Completed — nothing here is stored beyond the faction choice itself,
 * so the rank always stays in sync with the real watchlist.
 */
@Composable
private fun RankSection(
    faction: Faction,
    completed: Int,
    onFactionChange: (Faction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FactionSwitcher(selected = faction, onSelect = onFactionChange)
        Spacer(modifier = Modifier.height(12.dp))
        RankProgressCard(faction = faction, completed = completed)
        Spacer(modifier = Modifier.height(12.dp))
        RankLadderDropdown(faction = faction, completed = completed)
    }
}

@Composable
private fun FactionSwitcher(selected: Faction, onSelect: (Faction) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Faction.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RankProgressCard(faction: Faction, completed: Int, modifier: Modifier = Modifier) {
    val current = currentRank(faction, completed)
    val next = nextRank(faction, completed)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (current?.level ?: 0).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Bone
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = current?.title ?: "Unranked",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${faction.displayName} · Level ${current?.level ?: 0} of 7",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (next != null) {
                val prevThreshold = current?.completedRequired ?: 0
                val span = (next.completedRequired - prevThreshold).coerceAtLeast(1)
                val progress = ((completed - prevThreshold).toFloat() / span.toFloat()).coerceIn(0f, 1f)

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$completed / ${next.completedRequired} completed to become ${next.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Max rank reached — $completed completed titles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RankLadderDropdown(faction: Faction, completed: Int, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val tiers = remember(faction) { ranksFor(faction).sortedByDescending { it.level } }
    val currentLevel = currentRank(faction, completed)?.level

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Full Rank Ladder",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse rank ladder" else "Expand rank ladder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    tiers.forEach { tier ->
                        RankLadderRow(
                            tier = tier,
                            isUnlocked = completed >= tier.completedRequired,
                            isCurrent = tier.level == currentLevel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankLadderRow(tier: RankTier, isUnlocked: Boolean, isCurrent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = if (isCurrent) 1f else 0.35f)
                    else MaterialTheme.colorScheme.background
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = tier.level.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Bone
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Smoke,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tier.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isUnlocked) Bone else Smoke
            )
            Text(
                text = "${tier.completedRequired} completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isCurrent) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Current rank",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
