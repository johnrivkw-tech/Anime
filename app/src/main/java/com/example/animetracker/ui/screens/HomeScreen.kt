package com.example.animetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.animetracker.data.Anime
import com.example.animetracker.data.AnimeStatus
import com.example.animetracker.ui.components.AdaptiveAnimeGrid
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AnimeViewModel) {
    val animeList by viewModel.filteredAnime.collectAsState()
    val allAnime by viewModel.allLocalAnime.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var animeBeingEdited by remember { mutableStateOf<Anime?>(null) }
    var animePendingDelete by remember { mutableStateOf<Anime?>(null) }

    var searchExpanded by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            searchFocusRequester.requestFocus()
        }
    }

    val watchingCount = allAnime.count { it.status == AnimeStatus.WATCHING }
    val completedCount = allAnime.count { it.status == AnimeStatus.COMPLETED }
    val planCount = allAnime.count { it.status == AnimeStatus.PLAN_TO_WATCH }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header — title on the left, a search icon on the right that
            // expands into a filter field instead of a bar that's always
            // taking up space.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My List",
                        style = MaterialTheme.typography.titleLarge,
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
                IconButton(
                    onClick = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) viewModel.onSearchQueryChange("")
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (searchExpanded) "Close search" else "Search your list",
                        tint = Smoke
                    )
                }
            }

            AnimatedVisibility(
                visible = searchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .focusRequester(searchFocusRequester),
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
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = Bone,
                        unfocusedTextColor = Bone,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
            }

            // Filter tabs — flat text tabs with an underline on the active
            // one, in the vein of a streaming app's list-header tab strip.
            MyListFilterTabs(
                tabs = listOf(
                    ListTab("All", null, allAnime.size),
                    ListTab("Watching", AnimeStatus.WATCHING, watchingCount),
                    ListTab("Completed", AnimeStatus.COMPLETED, completedCount),
                    ListTab("Planning", AnimeStatus.PLAN_TO_WATCH, planCount)
                ),
                selected = statusFilter,
                onSelect = { viewModel.onStatusFilterChange(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            val isFiltering = searchQuery.isNotEmpty() || statusFilter != null

            if (animeList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), MaterialTheme.colorScheme.surface)
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
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
                                "Search for a title and set its status to add it here"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Smoke
                        )
                        if (!isFiltering) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    animeBeingEdited = null
                                    showDialog = true
                                }
                            ) {
                                Text(
                                    text = "Or add a title manually",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                AdaptiveAnimeGrid(
                    items = animeList,
                    key = { it.id },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 110.dp),
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 18.dp
                ) { anime ->
                    MyListPosterCard(
                        anime = anime,
                        onClick = {
                            animeBeingEdited = anime
                            showDialog = true
                        },
                        onLongClick = { animePendingDelete = anime }
                    )
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

    val deleteTarget = animePendingDelete
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { animePendingDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Remove anime?", color = Bone) },
            text = { Text("Remove \"${deleteTarget.name}\" from your watchlist? This can't be undone.", color = Smoke) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAnime(deleteTarget)
                    animePendingDelete = null
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { animePendingDelete = null }) {
                    Text("Cancel", color = Smoke)
                }
            }
        )
    }
}

/**
 * My List grid tile: just the poster art with the title in small text
 * underneath, nothing else. Tap opens the edit dialog (status/episodes/
 * rating all live there) and long-press asks to remove it — the old
 * wide row-style card doesn't fit a multi-column grid and was wrapping
 * its text one letter per line.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MyListPosterCard(
    anime: Anime,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        ) {
            if (anime.imageUrl != null) {
                AsyncImage(
                    model = anime.imageUrl,
                    contentDescription = anime.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = Smoke,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = anime.name,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp),
            fontWeight = FontWeight.Bold,
            color = Bone,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val progressText = if (anime.status == AnimeStatus.COMPLETED) {
                if (anime.totalEpisodes > 0) "Ep ${anime.totalEpisodes} · Completed" else "Completed"
            } else if (anime.totalEpisodes > 0) {
                "Ep ${anime.episodesWatched}/${anime.totalEpisodes}"
            } else {
                anime.status.label
            }
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options for ${anime.name}",
                        tint = Smoke,
                        modifier = Modifier.size(14.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = Smoke) },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        onClick = {
                            menuExpanded = false
                            onLongClick()
                        }
                    )
                }
            }
        }
    }
}

/** One entry in the My List filter tab strip. `status = null` means "All". */
private data class ListTab(val label: String, val status: AnimeStatus?, val count: Int)

/**
 * Flat text tab strip for switching the list filter — a plain label per
 * tab, bold + underlined when active, sitting on a hairline baseline that
 * runs the full width. Mirrors the tab row streaming apps use above their
 * "My List" grid, rather than the old boxed chip-with-icon stat tiles.
 */
@Composable
private fun MyListFilterTabs(
    tabs: List<ListTab>,
    selected: AnimeStatus?,
    onSelect: (AnimeStatus?) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.status == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .clickable { onSelect(if (isSelected) null else tab.status) }
                        .padding(top = 4.dp, start = 2.dp, end = 2.dp)
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Bone else Smoke
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(if (isSelected) 22.dp else 0.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
            }
        }
    }
}
