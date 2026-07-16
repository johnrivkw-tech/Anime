package com.example.animetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.animetracker.data.network.AniListMedia
import com.example.animetracker.data.network.JikanCharacter

/**
 * Full-screen search dialog for adding a title to the Profile screen's
 * curated "Favorite Anime" shelf. Visually mirrors [SearchAnimeDialog], but
 * selecting a result adds it straight to the favorites shelf instead of the
 * watchlist, and there's no "add manually" fallback since a favorite has to
 * be a real AniList entry.
 */
@Composable
fun FavoriteAnimePickerDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AniListMedia>,
    isLoading: Boolean,
    error: String?,
    alreadyPickedIds: Set<Int>,
    onDismiss: () -> Unit,
    onSelect: (AniListMedia) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                PickerHeader(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Search anime titles...",
                    onDismiss = onDismiss
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        error != null -> PickerMessage(error, isError = true)
                        query.isBlank() -> PickerMessage("Start typing to search anime")
                        results.isEmpty() -> PickerMessage("No results found")
                        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(results, key = { it.id }) { result ->
                                val alreadyPicked = result.id in alreadyPickedIds
                                FavoriteAnimeResultRow(
                                    result = result,
                                    alreadyPicked = alreadyPicked,
                                    onClick = { if (!alreadyPicked) onSelect(result) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen search dialog for adding a character to the Profile screen's
 * curated "Favorite Characters" shelf. Backed by a MyAnimeList character
 * search via Jikan, unlike the per-anime cast list used elsewhere (which
 * still uses AniList).
 */
@Composable
fun FavoriteCharacterPickerDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<JikanCharacter>,
    isLoading: Boolean,
    error: String?,
    alreadyPickedIds: Set<Int>,
    onDismiss: () -> Unit,
    onSelect: (JikanCharacter) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                PickerHeader(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Search MyAnimeList characters...",
                    onDismiss = onDismiss
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        error != null -> PickerMessage(error, isError = true)
                        query.isBlank() -> PickerMessage("Start typing to search characters")
                        results.isEmpty() -> PickerMessage("No results found")
                        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(results, key = { it.malId }) { result ->
                                val alreadyPicked = result.malId in alreadyPickedIds
                                FavoriteCharacterResultRow(
                                    result = result,
                                    alreadyPicked = alreadyPicked,
                                    onClick = { if (!alreadyPicked) onSelect(result) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onDismiss: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
private fun PickerMessage(text: String, isError: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FavoriteAnimeResultRow(result: AniListMedia, alreadyPicked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyPicked, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = result.posterUrl,
            contentDescription = result.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 56.dp, height = 80.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (alreadyPicked) "Already in your favorites" else result.status ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = if (alreadyPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavoriteCharacterResultRow(result: JikanCharacter, alreadyPicked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyPicked, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = result.imageUrl,
            contentDescription = result.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (alreadyPicked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Already in your favorites",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
