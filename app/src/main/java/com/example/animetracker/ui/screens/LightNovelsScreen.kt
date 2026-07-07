package com.example.animetracker.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.data.MangaEntity
import com.example.animetracker.data.network.MangaDexManga
import com.example.animetracker.ui.theme.Blaze
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Charcoal
import com.example.animetracker.ui.theme.CharcoalHigh
import com.example.animetracker.ui.theme.ErrorRed
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.ui.theme.Void
import com.example.animetracker.viewmodel.AnimeViewModel
import java.text.DateFormat
import java.util.Date

/**
 * "Reading" screen — two sub-tabs sharing one header:
 * - Novels: PDFs picked individually or from a linked folder (unchanged
 *   from before; see the folder-linking feature for details).
 * - Manga: search MangaDex, add titles to a local library, and tap through
 *   to a chapter list. [onMangaSelected] fires once loadChapters(...) has
 *   already been kicked off, so the caller just needs to navigate.
 */
@Composable
fun LightNovelsScreen(
    viewModel: AnimeViewModel,
    onMangaSelected: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
    ) {
        Text(
            text = "Reading",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Bone,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Void,
            contentColor = Blaze
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Novels") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Manga") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            NovelsTab(viewModel)
        } else {
            MangaTab(viewModel, onMangaSelected)
        }
    }
}

@Composable
private fun NovelsTab(viewModel: AnimeViewModel) {
    val context = LocalContext.current
    val novels by viewModel.lightNovels.collectAsState()
    val folderNovels by viewModel.folderNovels.collectAsState()
    val linkedFolderUri by viewModel.linkedFolderUri.collectAsState()
    val linkedFolderName by viewModel.linkedFolderName.collectAsState()

    fun openPdf(uri: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uri), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF reader app found to open this file.", Toast.LENGTH_SHORT).show()
        }
    }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Some providers don't support persistable permissions; the
            // file will still open now, it just might need re-picking
            // after a reboot. Not fatal either way.
        }

        val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment ?: "Untitled"

        viewModel.addLightNovel(title = displayName.removeSuffix(".pdf"), uri = uri.toString())
    }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri == null) return@rememberLauncherForActivityResult

        try {
            context.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Fine — it'll still work for this session even if it can't
            // survive a reboot.
        }

        viewModel.linkFolder(treeUri)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { pickFolderLauncher.launch(null) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CharcoalHigh)
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Link a folder", tint = Bone)
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { pickPdfLauncher.launch(arrayOf("application/pdf")) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blaze)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add a PDF", tint = Void)
            }
        }

        if (linkedFolderUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = CharcoalHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null, tint = Blaze, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Linked: ${linkedFolderName ?: "folder"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Bone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.scanLinkedFolder() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan folder", tint = Smoke)
                    }
                    IconButton(onClick = { viewModel.unlinkFolder() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Unlink folder", tint = Smoke)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (novels.isEmpty() && folderNovels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Smoke, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No light novels yet.\nAdd a PDF or link a folder above.",
                        color = Smoke,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(folderNovels, key = { "folder_" + it.uri }) { pdf ->
                    ReadingRow(
                        title = pdf.title,
                        subtitle = "From linked folder",
                        coverUrl = null,
                        onOpen = { openPdf(pdf.uri) },
                        onDelete = null
                    )
                }
                items(novels, key = { "manual_" + it.id }) { novel ->
                    ReadingRow(
                        title = novel.title,
                        subtitle = "Added ${remember(novel.addedAt) { DateFormat.getDateInstance().format(Date(novel.addedAt)) }}",
                        coverUrl = null,
                        onOpen = { openPdf(novel.uri) },
                        onDelete = { viewModel.removeLightNovel(novel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaTab(viewModel: AnimeViewModel, onMangaSelected: () -> Unit) {
    val query by viewModel.mangaSearchQuery.collectAsState()
    val results by viewModel.mangaSearchResults.collectAsState()
    val isSearching by viewModel.isMangaSearchLoading.collectAsState()
    val searchError by viewModel.mangaSearchError.collectAsState()
    val library by viewModel.mangaLibrary.collectAsState()

    val libraryIds = remember(library) { library.map { it.mangaDexId }.toSet() }

    fun selectManga(id: String, title: String) {
        viewModel.loadChapters(id, title)
        onMangaSelected()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onMangaSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search MangaDex...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.searchManga(query) }),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Bone,
                    unfocusedTextColor = Bone,
                    focusedContainerColor = CharcoalHigh,
                    unfocusedContainerColor = CharcoalHigh,
                    focusedIndicatorColor = Blaze,
                    unfocusedIndicatorColor = Charcoal
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.searchManga(query) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Blaze)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Void)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blaze)
                    }
                }
            }
            searchError?.let { error ->
                item {
                    Text(text = error, color = ErrorRed, modifier = Modifier.padding(12.dp))
                }
            }
            if (results.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Smoke,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(results, key = { "result_" + it.id }) { manga ->
                    MangaRow(
                        title = manga.displayTitle,
                        coverUrl = manga.coverUrl,
                        onOpen = { selectManga(manga.id, manga.displayTitle) },
                        trailingIcon = if (manga.id in libraryIds) Icons.Filled.Check else Icons.Filled.Add,
                        onTrailingClick = { if (manga.id !in libraryIds) viewModel.addMangaToLibrary(manga) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Smoke,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            if (library.isEmpty()) {
                item {
                    Text(
                        text = "Search above and add a title to start your library.",
                        color = Smoke,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(library, key = { "library_" + it.mangaDexId }) { manga ->
                    MangaRow(
                        title = manga.title,
                        coverUrl = manga.coverUrl,
                        onOpen = { selectManga(manga.mangaDexId, manga.title) },
                        trailingIcon = Icons.Filled.Delete,
                        onTrailingClick = { viewModel.removeMangaFromLibrary(manga) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaRow(
    title: String,
    coverUrl: String?,
    onOpen: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onTrailingClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen),
        color = CharcoalHigh,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 48.dp, height = 68.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Charcoal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Smoke)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Bone,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onTrailingClick) {
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = if (trailingIcon == Icons.Filled.Check) Smoke else Blaze
                )
            }
        }
    }
}

@Composable
private fun ReadingRow(
    title: String,
    subtitle: String,
    coverUrl: String?,
    onOpen: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen),
        color = CharcoalHigh,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Charcoal),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Blaze)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Bone,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Smoke)
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Smoke)
                }
            }
        }
    }
}
