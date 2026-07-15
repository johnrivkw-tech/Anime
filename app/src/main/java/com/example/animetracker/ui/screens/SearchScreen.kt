package com.example.animetracker.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.animetracker.data.network.ANILIST_GENRES
import com.example.animetracker.ui.components.AnimePosterCard
import com.example.animetracker.ui.model.toHomeCardItem
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel
import java.time.LocalDate

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
    val year by viewModel.discoverYear.collectAsState()
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

    var yearMenuExpanded by remember { mutableStateOf(false) }
    val currentYear = remember { LocalDate.now().year }
    val yearOptions = remember(currentYear) { (currentYear downTo currentYear - 14).toList() }

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
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onCatalogQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = season == null,
                        onClick = { viewModel.setDiscoverSeason(null) },
                        label = { Text("Any Season") },
                        shape = RoundedCornerShape(50),
                        colors = pillChipColors()
                    )
                    SEASONS.forEach { s ->
                        FilterChip(
                            selected = season == s,
                            onClick = { viewModel.setDiscoverSeason(if (season == s) null else s) },
                            label = { Text(seasonLabel(s)) },
                            shape = RoundedCornerShape(50),
                            colors = pillChipColors()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = yearMenuExpanded,
                        onExpandedChange = { yearMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = year?.toString() ?: "Any Year",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = Bone,
                                unfocusedTextColor = Bone,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = Smoke
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Any Year", color = Bone) },
                                onClick = {
                                    viewModel.setDiscoverYear(null)
                                    yearMenuExpanded = false
                                }
                            )
                            yearOptions.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString(), color = Bone) },
                                    onClick = {
                                        viewModel.setDiscoverYear(y)
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = genre == null,
                        onClick = { viewModel.setDiscoverGenre(null) },
                        label = { Text("All Genres") },
                        shape = RoundedCornerShape(50),
                        colors = pillChipColors()
                    )
                    ANILIST_GENRES.forEach { g ->
                        FilterChip(
                            selected = genre == g,
                            onClick = { viewModel.setDiscoverGenre(if (genre == g) null else g) },
                            label = { Text(g) },
                            shape = RoundedCornerShape(50),
                            colors = pillChipColors()
                        )
                    }
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
}

@Composable
private fun pillChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = Smoke,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = Bone
)
