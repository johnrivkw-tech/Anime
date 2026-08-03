package com.example.animetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.animetracker.data.network.DanbooruPost
import com.example.animetracker.data.network.DanbooruTagSuggestion
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.CharcoalHigh
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Pulse
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

/**
 * Fan-art gallery browser backed by Danbooru's public API.
  */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanbooruScreen(viewModel: AnimeViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var previewPost by remember { mutableStateOf<DanbooruPost?>(null) }

    val discoverResults by viewModel.danbooruDiscoverResults.collectAsState()
    val isDiscoverLoading by viewModel.isDanbooruDiscoverLoading.collectAsState()
    val isDiscoverLoadingMore by viewModel.isDanbooruDiscoverLoadingMore.collectAsState()
    val discoverCanLoadMore by viewModel.danbooruDiscoverCanLoadMore.collectAsState()
    val discoverError by viewModel.danbooruDiscoverError.collectAsState()

    val searchQuery by viewModel.danbooruSearchQuery.collectAsState()
    val searchResults by viewModel.danbooruSearchResults.collectAsState()
    val isSearchLoading by viewModel.isDanbooruSearchLoading.collectAsState()
    val isSearchLoadingMore by viewModel.isDanbooruSearchLoadingMore.collectAsState()
    val searchCanLoadMore by viewModel.danbooruSearchCanLoadMore.collectAsState()
    val searchError by viewModel.danbooruSearchError.collectAsState()
    val tagSuggestions by viewModel.danbooruTagSuggestions.collectAsState()

    val savedPosts by viewModel.danbooruSavedPosts.collectAsState()
    val savedIds by viewModel.danbooruSavedIds.collectAsState()

    LaunchedEffect(Unit) {
        if (discoverResults.isEmpty()) viewModel.loadDanbooruDiscover()
    }

    // Tapping a tag chip in the preview dialog jumps to Search with that tag.
    val onTagTapped: (String) -> Unit = { tag ->
        previewPost = null
        selectedTab = 1
        viewModel.searchDanbooruByTag(tag)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Gallery", fontWeight = FontWeight.Black, color = Bone) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Small, permanent reminder of the standing content rule —
            // not a toggle, just context for what's being shown.
            SafeContentBanner()

            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Discover") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Search") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Saved") }
                )
            }

            when (selectedTab) {
                0 -> DanbooruGrid(
                    posts = discoverResults,
                    isLoading = isDiscoverLoading,
                    isLoadingMore = isDiscoverLoadingMore,
                    canLoadMore = discoverCanLoadMore,
                    error = discoverError,
                    savedIds = savedIds,
                    onToggleSaved = viewModel::toggleDanbooruSaved,
                    onRetry = viewModel::loadDanbooruDiscover,
                    onLoadMore = viewModel::loadMoreDanbooruDiscover,
                    onPostClick = { previewPost = it }
                )
                1 -> Column(modifier = Modifier.fillMaxSize()) {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onDanbooruSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("Search tags, e.g. \"hatsune_miku\"", color = Smoke) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Smoke) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = viewModel::clearDanbooruSearch) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Smoke)
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onSearch = { viewModel.searchDanbooru(searchQuery) }
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Search
                            )
                        )
                        if (tagSuggestions.isNotEmpty()) {
                            TagSuggestionDropdown(
                                suggestions = tagSuggestions,
                                onSelect = viewModel::selectDanbooruTagSuggestion
                            )
                        }
                    }
                    if (searchQuery.isBlank()) {
                        DanbooruGrid(
                            posts = discoverResults,
                            isLoading = isDiscoverLoading,
                            isLoadingMore = isDiscoverLoadingMore,
                            canLoadMore = discoverCanLoadMore,
                            error = discoverError,
                            savedIds = savedIds,
                            onToggleSaved = viewModel::toggleDanbooruSaved,
                            onRetry = viewModel::loadDanbooruDiscover,
                            onLoadMore = viewModel::loadMoreDanbooruDiscover,
                            onPostClick = { previewPost = it }
                        )
                    } else {
                        DanbooruGrid(
                            posts = searchResults,
                            isLoading = isSearchLoading,
                            isLoadingMore = isSearchLoadingMore,
                            canLoadMore = searchCanLoadMore,
                            error = searchError,
                            savedIds = savedIds,
                            onToggleSaved = viewModel::toggleDanbooruSaved,
                            onRetry = { viewModel.searchDanbooru(searchQuery) },
                            onLoadMore = viewModel::loadMoreDanbooruSearch,
                            onPostClick = { previewPost = it }
                        )
                    }
                }
                2 -> DanbooruGrid(
                    posts = savedPosts,
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = false,
                    error = null,
                    emptyHint = "No saved images yet — tap the heart on any image to save it here",
                    savedIds = savedIds,
                    onToggleSaved = viewModel::toggleDanbooruSaved,
                    onRetry = {},
                    onLoadMore = {},
                    onPostClick = { previewPost = it }
                )
            }
        }
    }

    previewPost?.let { post ->
        DanbooruPreviewDialog(
            post = post,
            isSaved = post.id in savedIds,
            onToggleSaved = { viewModel.toggleDanbooruSaved(post) },
            onTagTapped = onTagTapped,
            onDismiss = { previewPost = null }
        )
    }
}

@Composable
private fun SafeContentBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Charcoal)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = Smoke,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.padding(start = 6.dp))
            Text(
                text = "General & sensitive-rated art only",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke
            )
        }
    }
}

@Composable
private fun TagSuggestionDropdown(
    suggestions: List<DanbooruTagSuggestion>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalHigh)
    ) {
        suggestions.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(suggestion.name) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion.displayName,
                    color = Bone,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = suggestion.post_count.toString(),
                    color = Smoke,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DanbooruGrid(
    posts: List<DanbooruPost>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    error: String?,
    emptyHint: String? = null,
    savedIds: Set<Long>,
    onToggleSaved: (DanbooruPost) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (DanbooruPost) -> Unit
) {
    when {
        isLoading && posts.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        error != null && posts.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error, color = ErrorRed, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                Text(
                    "Tap to retry",
                    color = Smoke,
                    modifier = Modifier.clickable(onClick = onRetry)
                )
            }
        }
        posts.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                emptyHint ?: "Nothing here yet",
                color = Smoke,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                DanbooruThumbnail(
                    post = post,
                    isSaved = post.id in savedIds,
                    onClick = { onPostClick(post) },
                    onToggleSaved = { onToggleSaved(post) }
                )
            }

            if (canLoadMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadMoreFooter(isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)
                }
            }
        }
    }
}

@Composable
private fun LoadMoreFooter(isLoadingMore: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoadingMore) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        } else {
            Button(onClick = onLoadMore) {
                Text("Load more")
            }
        }
    }
}

@Composable
private fun DanbooruThumbnail(
    post: DanbooruPost,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Charcoal)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = post.thumbnailUrl,
            contentDescription = post.tag_string_character.ifBlank { "Art" },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (post.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Charcoal.copy(alpha = 0.75f))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Video",
                    tint = Bone,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        IconButton(
            onClick = onToggleSaved,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(30.dp)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Charcoal.copy(alpha = 0.75f))
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isSaved) "Remove from saved" else "Save",
                tint = if (isSaved) Pulse else Bone,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DanbooruPreviewDialog(
    post: DanbooruPost,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onTagTapped: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Charcoal)
        ) {
            Box {
                val aspect = if (post.image_width > 0 && post.image_height > 0) {
                    (post.image_width.toFloat() / post.image_height.toFloat()).coerceIn(0.5f, 1.9f)
                } else {
                    1f
                }

                if (post.isVideo && post.fullImageUrl != null) {
                    DanbooruVideoPlayer(
                        url = post.fullImageUrl!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                    )
                } else {
                    AsyncImage(
                        model = post.fullImageUrl,
                        contentDescription = post.tag_string_character.ifBlank { "Art" },
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save",
                            tint = if (isSaved) Pulse else Bone
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Bone)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                if (post.characters.isNotEmpty()) {
                    Text(
                        text = post.characters.joinToString(", ") { it.replace('_', ' ') },
                        color = Bone,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (post.artists.isNotEmpty()) {
                    Text(
                        text = "by " + post.artists.joinToString(", ") { it.replace('_', ' ') },
                        color = Smoke,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (post.allTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.allTags.take(20).forEach { tag ->
                            TagChip(tag = tag, onClick = { onTagTapped(tag) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag.replace('_', ' '),
            color = Smoke,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Plays a Danbooru video post (webm/mp4) in-place with sound via ExoPlayer.
 * Autoplays and loops while the preview dialog is open; the player is
 * released as soon as the composable leaves composition (dialog dismissed).
 */
@Composable
private fun DanbooruVideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}
