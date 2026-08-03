package com.example.animetracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.animetracker.data.network.DanbooruPost
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.ErrorRed
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
    val discoverError by viewModel.danbooruDiscoverError.collectAsState()

    val searchQuery by viewModel.danbooruSearchQuery.collectAsState()
    val searchResults by viewModel.danbooruSearchResults.collectAsState()
    val isSearchLoading by viewModel.isDanbooruSearchLoading.collectAsState()
    val searchError by viewModel.danbooruSearchError.collectAsState()

    LaunchedEffect(Unit) {
        if (discoverResults.isEmpty()) viewModel.loadDanbooruDiscover()
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
            }

            when (selectedTab) {
                0 -> DanbooruGrid(
                    posts = discoverResults,
                    isLoading = isDiscoverLoading,
                    error = discoverError,
                    onRetry = viewModel::loadDanbooruDiscover,
                    onPostClick = { previewPost = it }
                )
                1 -> Column(modifier = Modifier.fillMaxSize()) {
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
                    DanbooruGrid(
                        posts = searchResults,
                        isLoading = isSearchLoading,
                        error = searchError,
                        emptyHint = if (searchQuery.isBlank()) "Search a tag to get started" else null,
                        onRetry = { viewModel.searchDanbooru(searchQuery) },
                        onPostClick = { previewPost = it }
                    )
                }
            }
        }
    }

    previewPost?.let { post ->
        DanbooruPreviewDialog(post = post, onDismiss = { previewPost = null })
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
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = Smoke,
                modifier = Modifier.size(16.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 6.dp))
            Text(
                text = "Explicit-rated art",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke
            )
        }
    }
}

@Composable
private fun DanbooruGrid(
    posts: List<DanbooruPost>,
    isLoading: Boolean,
    error: String?,
    emptyHint: String? = null,
    onRetry: () -> Unit,
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
            Text(emptyHint ?: "Nothing here yet", color = Smoke)
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                DanbooruThumbnail(post = post, onClick = { onPostClick(post) })
            }
        }
    }
}

@Composable
private fun DanbooruThumbnail(post: DanbooruPost, onClick: () -> Unit) {
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
    }
}

@Composable
private fun DanbooruPreviewDialog(post: DanbooruPost, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Charcoal)
        ) {
            Box {
                AsyncImage(
                    model = post.fullImageUrl,
                    contentDescription = post.tag_string_character.ifBlank { "Art" },
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Bone)
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
            }
        }
    }
}
