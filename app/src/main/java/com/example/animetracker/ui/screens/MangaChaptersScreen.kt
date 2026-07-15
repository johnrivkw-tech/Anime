package com.example.animetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel

/**
 * Chapter list for whichever manga was last selected (see
 * AnimeViewModel.loadChapters — it's called right before navigating here,
 * so no manga ID needs to travel through the nav route itself).
 */
@Composable
fun MangaChaptersScreen(
    viewModel: AnimeViewModel,
    onChapterClick: () -> Unit,
    onBack: () -> Unit
) {
    val title by viewModel.selectedMangaTitle.collectAsState()
    val chapters by viewModel.mangaChapters.collectAsState()
    val isLoading by viewModel.isChaptersLoading.collectAsState()
    val error by viewModel.chaptersError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
            }
            Text(
                text = title ?: "Chapters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Bone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "", color = ErrorRed, modifier = Modifier.padding(24.dp))
            }
            chapters.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Smoke)
                    Text(
                        text = "No English chapters found for this title.",
                        color = Smoke,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chapters, key = { it.id }) { chapter ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loadChapterPages(chapter.id)
                                onChapterClick()
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = chapter.attributes.displayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Bone,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
