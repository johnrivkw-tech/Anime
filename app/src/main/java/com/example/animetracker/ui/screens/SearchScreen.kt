package com.example.animetracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.animetracker.data.network.ANILIST_GENRES
import com.example.animetracker.ui.components.AnimePosterCard
import com.example.animetracker.ui.model.toHomeCardItem
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

private val SEASONS = listOf("WINTER", "SPRING", "SUMMER", "FALL")

private fun seasonLabel(value: String?): String = when (value) {
    "WINTER" -> "Winter"
    "SPRING" -> "Spring"
    "SUMMER" -> "Summer"
    "FALL" -> "Fall"
    else -> "Any Season"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit, onBack: () -> Unit) {
    val query by viewModel.catalogQuery.collectAsState()
    val catalogResults by viewModel.catalogResults.collectAsState()
    val isCatalogSearching by viewModel.isCatalogSearching.collectAsState()
    val catalogError by viewModel.catalogError.collectAsState()

    val genre by viewModel.discoverGenre.collectAsState()
    val season by viewModel.discoverSeason.collectAsState()
    val discoverResults by viewModel.discoverResults.collectAsState()
    val isDiscoverLoading by viewModel.isDiscoverLoading.collectAsState()
    val discoverError by viewModel.discoverError.collectAsState()

    val localByAniListId by viewModel.localByAniListId.collectAsState()

    val isSearching = query.isNotBlank()

    val catalogItems = remember(catalogResults, localByAniListId) {
        catalogResults.map { it.toHomeCardItem(localByAniListId[it.id]) }
    }
    val discoverItems = remember(discoverResults, localByAniListId) {
        discoverResults.map { it.toHomeCardItem(localByAniListId[it.id]) }
    }

    var filterSheetOpen by remember { mutableStateOf(false) }
    val activeFilterCount = listOfNotNull(season, genre).size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = Bone)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onCatalogQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search the anime catalog...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onCatalogQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )

                if (!isSearching) {
                    FilterButton(
                        activeCount = activeFilterCount,
                        onClick = { filterSheetOpen = true }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isSearching) {
                    when {
                        isCatalogSearching -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
                        }
                        catalogError != null -> {
                            Text(
                                text = catalogError ?: "",
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        catalogItems.isEmpty() -> {
                            Text(
                                text = "No results found",
                                modifier = Modifier.align(Alignment.Center),
                                color = Smoke
                            )
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(128.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(catalogItems, key = { it.key }) { item ->
                                    AnimePosterCard(item = item, onClick = { item.aniListId?.let(onAnimeClick) })
                                }
                            }
                        }
                    }
                } else {
                    when {
                        isDiscoverLoading && discoverItems.isEmpty() -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
                        }
                        discoverError != null && discoverItems.isEmpty() -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = discoverError ?: "",
                                    color = ErrorRed,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadDiscover() },
                                    modifier = Modifier.padding(top = 12.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Bone)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                        discoverItems.isEmpty() -> {
                            Text(
                                text = "No anime match these filters",
                                modifier = Modifier.align(Alignment.Center),
                                color = Smoke
                            )
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(128.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(discoverItems, key = { it.key }) { item ->
                                    AnimePosterCard(item = item, onClick = { item.aniListId?.let(onAnimeClick) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (filterSheetOpen) {
        FilterDialog(
            season = season,
            genre = genre,
            onSeasonChange = viewModel::setDiscoverSeason,
            onGenreChange = viewModel::setDiscoverGenre,
            onClearAll = {
                viewModel.setDiscoverSeason(null)
                viewModel.setDiscoverGenre(null)
            },
            onDismiss = { filterSheetOpen = false }
        )
    }
}

/** Single entry point for every discover filter — replaces the row of always-visible
 *  season/year/genre controls with one button that opens them in a dialog, and shows
 *  a small badge when filters are active so it's clear at a glance. */
@Composable
private fun FilterButton(activeCount: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Bone),
        modifier = Modifier.height(56.dp)
    ) {
        Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Text("Filters")
        if (activeCount > 0) {
            Spacer(modifier = Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$activeCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Bone,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    season: String?,
    genre: String?,
    onSeasonChange: (String?) -> Unit,
    onGenreChange: (String?) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Bone
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Smoke)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Season", style = MaterialTheme.typography.labelLarge, color = Smoke)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = season == null,
                        onClick = { onSeasonChange(null) },
                        label = { Text("Any Season") },
                        shape = RoundedCornerShape(50),
                        colors = pillChipColors()
                    )
                    SEASONS.forEach { s ->
                        FilterChip(
                            selected = season == s,
                            onClick = { onSeasonChange(if (season == s) null else s) },
                            label = { Text(seasonLabel(s)) },
                            shape = RoundedCornerShape(50),
                            colors = pillChipColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Genre", style = MaterialTheme.typography.labelLarge, color = Smoke)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = genre == null,
                        onClick = { onGenreChange(null) },
                        label = { Text("All Genres") },
                        shape = RoundedCornerShape(50),
                        colors = pillChipColors()
                    )
                    ANILIST_GENRES.forEach { g ->
                        FilterChip(
                            selected = genre == g,
                            onClick = { onGenreChange(if (genre == g) null else g) },
                            label = { Text(g) },
                            shape = RoundedCornerShape(50),
                            colors = pillChipColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClearAll) {
                        Text("Clear all", color = Smoke)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Bone)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun pillChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = Smoke,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = Bone
)
