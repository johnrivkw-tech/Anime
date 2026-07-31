package com.example.animetracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.R
import com.example.animetracker.ui.model.HomeCardItem
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke

@Composable
fun FeaturedBanner(
    items: List<HomeCardItem>,
    onClick: (HomeCardItem) -> Unit,
    onAiClick: () -> Unit,
    onReadingClick: () -> Unit,
    onGamesClick: () -> Unit,
    onProfileClick: () -> Unit,
    profileAvatarPath: String? = null,
    modifier: Modifier = Modifier
) {
    // 50% of screen height hero banner, leaving the rest of the page
    // visible below to hint that there's more to scroll to.
    val bannerHeight = (LocalConfiguration.current.screenHeightDp * 0.6f).dp

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
                        .fillMaxWidth()
                        .fillMaxHeight(0.42f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.Black.copy(alpha = 0.55f),
                                    0.8f to Color.Black.copy(alpha = 0.9f),
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

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displayLarge,
                        color = Bone,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Meta line: age rating pill + genres, mirroring the
                    // "16+ • Dub|Sub • Action, Supernatural..." treatment.
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Smoke.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isAdult) "18+" else "13+",
                                style = MaterialTheme.typography.labelSmall,
                                color = Bone
                            )
                        }
                        Text(
                            text = "  •  Sub",
                            style = MaterialTheme.typography.bodySmall,
                            color = Smoke
                        )
                        if (item.genres.isNotEmpty()) {
                            Text(
                                text = "  •  " + item.genres.take(4).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Smoke,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Short synopsis, truncated the same way a streaming
                    // hero card clips its description to a couple of lines.
                    if (!item.synopsis.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.synopsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Bone.copy(alpha = 0.85f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Page indicator dots, sit just above the bottom edge of the banner.
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(items.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isSelected) 22.dp else 14.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Bone.copy(alpha = 0.35f))
                    )
                }
            }
        }

        // Top bar: brand mark top-left, utility icons top-right — matching
        // the reference app's plain, no-background icon treatment.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.straw_hat_logo),
                contentDescription = "Rei",
                modifier = Modifier.size(52.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
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
                        DropdownMenuItem(
                            text = { Text("Games") },
                            leadingIcon = { Icon(Icons.Filled.Casino, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                menuExpanded = false
                                onGamesClick()
                            }
                        )
                    }
                }
                // Profile entry point — shows whichever avatar the user has
                // chosen, falling back to a plain person glyph until one is
                // set. Tapping it opens the profile screen.
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileAvatarPath != null) {
                        AsyncImage(
                            model = profileAvatarPath,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Bone,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
    }
}
