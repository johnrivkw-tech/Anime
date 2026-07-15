package com.example.animetracker.ui.screens
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

/**
 * Webtoon-style reader: every page of the chapter stacked in one
 * continuous vertical scroll, full width, no paging gestures — just keep
 * scrolling down. Pages come from AnimeViewModel.loadChapterPages, called
 * right before navigating here (see MangaChaptersScreen).
 */
@Composable
fun MangaReaderScreen(
    viewModel: AnimeViewModel,
    onBack: () -> Unit
) {
    val pages by viewModel.chapterPages.collectAsState()
    val isLoading by viewModel.isPagesLoading.collectAsState()
    val error by viewModel.pagesError.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(text = error ?: "", color = ErrorRed)
                }
            }
            pages.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No pages found for this chapter.", color = Smoke)
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(pages) { pageUrl ->
                    AsyncImage(
                        model = pageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
        }
    }
}
