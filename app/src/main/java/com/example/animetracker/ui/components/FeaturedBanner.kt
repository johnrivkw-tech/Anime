package com.example.animetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.animetracker.ui.model.HomeCardItem
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke

@Composable
fun FeaturedBanner(
    items: List<HomeCardItem>,
    onClick: (HomeCardItem) -> Unit,
    onAiClick: () -> Unit,
    onReadingClick: () -> Unit,
    onProfileClick: () -> Unit,
    profileAvatarPath: String? = null,
    modifier: Modifier = Modifier
) {
    // 50% of screen height hero banner, leaving the rest of the page
    // visible below to hint that there's more to scroll to.
    val bannerHeight = (LocalConfiguration.current.screenHeightDp * 0.5f).dp

    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = items[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick(item) }
            ) {
                AsyncImage(
                    model = item.imageUrl ?: item.bannerUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom fade: starts subtle, ends fully opaque so the banner
                // blends seamlessly into the page background below it instead
                // of showing a hard edge where the image ends.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.45f to Color.Black.copy(alpha = 0.55f),
                                    0.75f to Color.Black.copy(alpha = 0.92f),
                                    1.0f to MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
                // Subtle top scrim so status-bar icons stay legible over the
                // artwork now that the banner extends behind them.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displayLarge,
                        color = Bone,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.genres.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Smoke,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Page indicator dots, sit just above the title block.
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(items.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Bone.copy(alpha = 0.4f))
                    )
                }
            }
        }

        // Top bar (wordmark, menu, profile) stays fixed above the pager.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VizoraWordmark(fontSize = 26.sp, markSize = 26.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Bone
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lena AI") },
                            leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                menuExpanded = false
                                onAiClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reading") },
                            leadingIcon = { Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                menuExpanded = false
                                onReadingClick()
                            }
                        )
                    }
                }
                // Profile entry point — shows whichever avatar the user has
                // chosen, falling back to a plain person glyph until one is
                // set. Tapping it opens the profile screen.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, Bone.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileAvatarPath != null) {
                        AsyncImage(
                            model = profileAvatarPath,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Bone,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
