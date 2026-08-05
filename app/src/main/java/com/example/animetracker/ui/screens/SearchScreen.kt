
package com.example.animetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animetracker.data.network.ANILIST_GENRES
import com.example.animetracker.data.network.ANILIST_HENTAI_GENRE
import com.example.animetracker.data.network.AniListMedia
import com.example.animetracker.ui.components.AdaptiveAnimeGrid
import com.example.animetracker.ui.components.FallbackNotice
import com.example.animetracker.ui.components.AnimeGridCard
import com.example.animetracker.ui.model.toHomeCardItem
import com.example.animetracker.ui.model.textStyle
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

@Composable
fun SearchScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit) {
    val query by viewModel.catalogQuery.collectAsState()
    val catalogResults by viewModel.catalogResults.collectAsState()
    val isCatalogSearching by viewModel.isCatalogSearching.collectAsState()
    val catalogError by viewModel.catalogError.collectAsState()

    val genre by viewModel.discoverGenre.collectAsState()
    val discoverResults by viewModel.discoverResults.collectAsState()
    val isDiscoverLoading by viewModel.isDiscoverLoading.collectAsState()
    val discoverError by viewModel.discoverError.collectAsState()

    val mangaTitleResults by viewModel.mangaTitleResults.collectAsState()
    val mangaLibrary by viewModel.mangaLibrary.collectAsState()
    val addedMangaKeys = remember(mangaLibrary) { mangaLibrary.map { it.mangaDexId }.toSet() }

    val localByAniListId by viewModel.localByAniListId.collectAsState()
    val usingFallback by viewModel.searchUsingFallback.collectAsState()

    // matureContentEnabled is already the combined "age verified as 18+ AND
    // opted in to mature content" flag the rest of the app gates adult
    // content behind — reusing it here means Hentai only ever appears in
    // the genre strip once both conditions are true, and disappears again
    // immediately (list rebuilds, selection included) if the user turns
    // either one off.
    val matureContentEnabled by viewModel.matureContentEnabled.collectAsState()
    val searchGenres = remember(matureContentEnabled) {
        if (matureContentEnabled) ANILIST_GENRES + ANILIST_HENTAI_GENRE else ANILIST_GENRES
    }
    // If mature content gets turned off from Settings while Hentai is the
    // active selection, drop back to "All Genres" instead of leaving the
    // filter silently pinned to a tab that's no longer visible anywhere.
    LaunchedEffect(matureContentEnabled) {
        if (!matureContentEnabled && genre == ANILIST_HENTAI_GENRE) {
            viewModel.setDiscoverGenre(null)
        }
    }

    val isSearching = query.isNotBlank()

    val catalogItems = remember(catalogResults, localByAniListId) {
        catalogResults.map { it.toHomeCardItem(localByAniListId[it.id]) }
    }
    val discoverItems = remember(discoverResults, localByAniListId) {
        discoverResults.map { it.toHomeCardItem(localByAniListId[it.id]) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Nested inside the app-level Scaffold in MainActivity, which
        // already reserves space for the floating bottom nav bar. Leaving
        // this Scaffold's default contentWindowInsets in place reserved
        // the system nav-bar area a *second* time, which is what left the
        // large empty gap at the bottom of Search — zeroing it out here
        // matches the fix already applied in Settings.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header — same title style/spacing as My List and Schedule
            // (titleLarge/Black, 16dp horizontal/10dp vertical) so all four
            // tabs read as one consistent app rather than three different
            // ones stitched together. No back button: Search is a bottom
            // nav tab like the others, not a screen you drill into.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Search",
                    style = titleGradient.textStyle(
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Bone
                        )
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onCatalogQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = {
                        Text(
                            text = "Search the anime catalog...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
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
            }

            if (usingFallback) {
                FallbackNotice(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // Manga titles from AniList — only ever populated when the
            // Settings "Show AniList Manga" toggle is on, and only while
            // actively typing a search (never shown browsing genres).
            // Deliberately just a row of tappable title chips, not a full
            // poster grid, per how this feature is meant to work: titles
            // only, tap to save into the Manga list in Settings.
            if (isSearching && mangaTitleResults.isNotEmpty()) {
                MangaTitleStrip(
                    results = mangaTitleResults,
                    addedKeys = addedMangaKeys,
                    onTap = { viewModel.addAniListMangaToLibrary(it) }
                )
            }

            // Genre tabs replace the old Filter button + dialog entirely —
            // tap a genre directly, same flat-tab-strip style as My List's
            // All/Watching/Completed/Planning row, just horizontally
            // scrollable since there are ~18 genres instead of 4. These
            // only affect the browse/discover grid: once you're actually
            // typing a search, genre has no effect on catalog results, so
            // the strip hides itself the same way the old Filter button did.
            if (!isSearching) {
                GenreFilterTabs(
                    genres = searchGenres,
                    selected = genre,
                    onSelect = viewModel::setDiscoverGenre
                )
            }

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val availableHeight = maxHeight
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
                            AdaptiveAnimeGrid(
                                items = catalogItems,
                                key = { it.key },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = availableHeight),
                                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 20.dp),
                                horizontalSpacing = 10.dp,
                                verticalSpacing = 18.dp
                            ) { item ->
                                AnimeGridCard(item = item, onClick = { item.aniListId?.let(onAnimeClick) })
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
                                text = "No anime match this genre",
                                modifier = Modifier.align(Alignment.Center),
                                color = Smoke
                            )
                        }
                        else -> {
                            AdaptiveAnimeGrid(
                                items = discoverItems,
                                key = { it.key },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = availableHeight),
                                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 20.dp),
                                horizontalSpacing = 10.dp,
                                verticalSpacing = 18.dp
                            ) { item ->
                                AnimeGridCard(item = item, onClick = { item.aniListId?.let(onAnimeClick) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Horizontally scrolling row of manga title chips — titles only, per the
 * Settings toggle's design: no covers, no synopsis, just tap a title to
 * save it into the Manga list. A checkmark replaces the chip's leading
 * dot once that title's already in the library, so re-tapping is a
 * harmless no-op-looking confirmation rather than silently doing nothing.
 */
@Composable
private fun MangaTitleStrip(
    results: List<AniListMedia>,
    addedKeys: Set<String>,
    onTap: (AniListMedia) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        results.forEach { media ->
            val isAdded = "anilist:${media.id}" in addedKeys
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = !isAdded) { onTap(media) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (isAdded) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Added to Manga list",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = media.displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAdded) MaterialTheme.colorScheme.primary else Bone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Flat text tab strip for genres — visually identical to My List's
 * All/Watching/Completed/Planning strip (bold + underlined label when
 * active, hairline baseline underneath), just scrollable horizontally
 * since ~18 genres won't fit on screen at once the way 4 status tabs do.
 */
@Composable
private fun GenreFilterTabs(
    genres: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
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
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            GenreTab(
                label = "All Genres",
                isSelected = selected == null,
                onClick = { onSelect(null) }
            )
            genres.forEach { g ->
                GenreTab(
                    label = g,
                    isSelected = selected == g,
                    onClick = { onSelect(if (selected == g) null else g) }
                )
            }
        }
    }
}

@Composable
private fun GenreTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .clickable(onClick = onClick)
            .padding(top = 4.dp, start = 2.dp, end = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Bone else Smoke,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
