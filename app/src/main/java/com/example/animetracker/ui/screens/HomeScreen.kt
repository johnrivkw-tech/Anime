package com.example.animetracker.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animetracker.data.Anime
import com.example.animetracker.data.AnimeStatus
import com.example.animetracker.ui.components.AnimeCard
import com.example.animetracker.ui.components.statusColor
import com.example.animetracker.ui.components.statusIcon
import com.example.animetracker.ui.theme.Blaze
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.CharcoalHigh
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.ui.theme.Void
import com.example.animetracker.viewmodel.AnimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AnimeViewModel) {
    val animeList by viewModel.filteredAnime.collectAsState()
    val allAnime by viewModel.allLocalAnime.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    val onlineResults by viewModel.searchResults.collectAsState()
    val isSearchingApi by viewModel.isSearchingApi.collectAsState()
    val searchApiError by viewModel.searchApiError.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var animeBeingEdited by remember { mutableStateOf<Anime?>(null) }

    var showSearchDialog by remember { mutableStateOf(false) }
    var onlineQuery by remember { mutableStateOf("") }

    fun closeSearchDialog() {
        showSearchDialog = false
        onlineQuery = ""
        viewModel.clearSearchResults()
    }

    val watchingCount = allAnime.count { it.status == AnimeStatus.WATCHING }
    val completedCount = allAnime.count { it.status == AnimeStatus.COMPLETED }
    val planCount = allAnime.count { it.status == AnimeStatus.PLAN_TO_WATCH }

    Scaffold(
        containerColor = Void,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSearchDialog = true },
                containerColor = Blaze,
                contentColor = Bone,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Anime", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "My List",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Bone
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (allAnime.isEmpty()) {
                        "Your collection is waiting to be filled"
                    } else {
                        "${allAnime.size} ${if (allAnime.size == 1) "title" else "titles"} in your collection"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Smoke
                )
            }

            // Stat tiles — tap one to jump straight to that filter.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    label = "Watching",
                    count = watchingCount,
                    status = AnimeStatus.WATCHING,
                    selected = statusFilter == AnimeStatus.WATCHING,
                    onClick = {
                        viewModel.onStatusFilterChange(
                            if (statusFilter == AnimeStatus.WATCHING) null else AnimeStatus.WATCHING
                        )
                    }
                )
                StatTile(
                    label = "Completed",
                    count = completedCount,
                    status = AnimeStatus.COMPLETED,
                    selected = statusFilter == AnimeStatus.COMPLETED,
                    onClick = {
                        viewModel.onStatusFilterChange(
                            if (statusFilter == AnimeStatus.COMPLETED) null else AnimeStatus.COMPLETED
                        )
                    }
                )
                StatTile(
                    label = "Plan to Watch",
                    count = planCount,
                    status = AnimeStatus.PLAN_TO_WATCH,
                    selected = statusFilter == AnimeStatus.PLAN_TO_WATCH,
                    onClick = {
                        viewModel.onStatusFilterChange(
                            if (statusFilter == AnimeStatus.PLAN_TO_WATCH) null else AnimeStatus.PLAN_TO_WATCH
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Search your watchlist", color = Smoke) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Smoke) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Smoke)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Charcoal,
                    unfocusedContainerColor = Charcoal,
                    focusedBorderColor = Blaze,
                    unfocusedBorderColor = Charcoal,
                    focusedTextColor = Bone,
                    unfocusedTextColor = Bone,
                    cursorColor = Blaze
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.onStatusFilterChange(null) },
                    label = { Text("All", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CharcoalHigh,
                        labelColor = Smoke,
                        selectedContainerColor = Blaze,
                        selectedLabelColor = Bone
                    )
                )
                AnimeStatus.entries.forEach { status ->
                    val color = statusColor(status)
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        label = { Text(status.label, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = statusIcon(status),
                                contentDescription = null,
                                modifier = Modifier.width(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CharcoalHigh,
                            labelColor = Smoke,
                            iconColor = Smoke,
                            selectedContainerColor = color,
                            selectedLabelColor = Bone,
                            selectedLeadingIconColor = Bone
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val isFiltering = searchQuery.isNotEmpty() || statusFilter != null

            if (animeList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Blaze.copy(alpha = 0.20f), Charcoal)
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoLibrary,
                                contentDescription = null,
                                tint = Blaze,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isFiltering) "No matches" else "Your watchlist is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Bone
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFiltering) {
                                "Try a different search or filter"
                            } else {
                                "Tap + Add Anime to build your collection"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Smoke
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animeList, key = { it.id }) { anime ->
                        AnimeCard(
                            anime = anime,
                            onIncrement = { viewModel.incrementEpisode(anime) },
                            onEdit = {
                                animeBeingEdited = anime
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteAnime(anime) },
                            onToggleFavorite = { viewModel.toggleFavorite(anime) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        val editing = animeBeingEdited
        AddEditAnimeDialog(
            anime = editing,
            onDismiss = { showDialog = false },
            onConfirm = { name, watched, total, status, rating ->
                if (editing != null) {
                    viewModel.updateAnime(
                        editing.copy(
                            name = name,
                            episodesWatched = watched,
                            totalEpisodes = total,
                            status = status,
                            rating = rating
                        )
                    )
                } else {
                    viewModel.addAnime(name, watched, total, status, rating)
                }
                showDialog = false
            }
        )
    }

    if (showSearchDialog) {
        SearchAnimeDialog(
            query = onlineQuery,
            onQueryChange = {
                onlineQuery = it
                viewModel.searchOnline(it)
            },
            results = onlineResults,
            isLoading = isSearchingApi,
            error = searchApiError,
            onDismiss = { closeSearchDialog() },
            onSelect = { result ->
                viewModel.addAnimeFromSearchResult(result)
                closeSearchDialog()
            },
            onAddManually = {
                closeSearchDialog()
                animeBeingEdited = null
                showDialog = true
            }
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    count: Int,
    status: AnimeStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = statusColor(status)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) color.copy(alpha = 0.18f) else Charcoal,
        modifier = Modifier.width(112.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = statusIcon(status),
                contentDescription = null,
                tint = color,
                modifier = Modifier.width(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Bone
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                maxLines = 1
            )
        }
    }
}
