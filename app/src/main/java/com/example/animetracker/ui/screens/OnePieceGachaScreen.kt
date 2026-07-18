package com.example.animetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.animetracker.ui.model.GachaCharacter
import com.example.animetracker.ui.model.GachaRarity
import com.example.animetracker.ui.model.GACHA_SINGLE_PULL_COST
import com.example.animetracker.ui.model.GACHA_TEN_PULL_COST
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel
import java.util.Locale

/**
 * The One Piece berries gacha: spend berries earned from watching to pull
 * characters across five rarity tiers (Common through Mythic), pure RNG,
 * no pity system. Roster and rarity come from live AniList data, ranked by
 * favourites — see [com.example.animetracker.ui.model.assignGachaTiers].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnePieceGachaScreen(
    viewModel: AnimeViewModel,
    onBack: () -> Unit
) {
    val roster by viewModel.gachaRoster.collectAsState()
    val isLoading by viewModel.isGachaRosterLoading.collectAsState()
    val error by viewModel.gachaRosterError.collectAsState()
    val owned by viewModel.gachaOwnedCounts.collectAsState()
    val availableBerries by viewModel.gachaAvailableBerries.collectAsState()
    val lastPull by viewModel.gachaLastPull.collectAsState()

    var insufficientFundsMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadGachaRoster() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("One Piece Gacha", fontWeight = FontWeight.Bold, color = Bone) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Bone)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading && roster.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    error != null && roster.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(error ?: "", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadGachaRoster(forceRefresh = true) }) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        BerriesBar(availableBerries)

                        PullButtonsRow(
                            canAffordSingle = availableBerries >= GACHA_SINGLE_PULL_COST,
                            canAffordTen = availableBerries >= GACHA_TEN_PULL_COST,
                            onPullSingle = {
                                insufficientFundsMessage = null
                                if (!viewModel.pullGacha(1)) {
                                    insufficientFundsMessage = "Not enough berries \u2014 watch more episodes to earn them."
                                }
                            },
                            onPullTen = {
                                insufficientFundsMessage = null
                                if (!viewModel.pullGacha(10)) {
                                    insufficientFundsMessage = "Not enough berries \u2014 watch more episodes to earn them."
                                }
                            }
                        )

                        AnimatedVisibility(visible = insufficientFundsMessage != null) {
                            Text(
                                text = insufficientFundsMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        RarityOddsRow()

                        CollectionGrid(roster = roster, owned = owned)
                    }
                }
            }
        }

        if (lastPull.isNotEmpty()) {
            PullRevealDialog(pulled = lastPull, onDismiss = { viewModel.clearGachaLastPull() })
        }
    }
}

@Composable
private fun BerriesBar(availableBerries: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${String.format(Locale.US, "%,d", availableBerries)} berries available",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Bone
        )
    }
}

@Composable
private fun PullButtonsRow(
    canAffordSingle: Boolean,
    canAffordTen: Boolean,
    onPullSingle: () -> Unit,
    onPullTen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPullSingle,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAffordSingle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pull x1", fontWeight = FontWeight.Bold)
                Text("${GACHA_SINGLE_PULL_COST} berries", style = MaterialTheme.typography.labelSmall)
            }
        }
        Button(
            onClick = onPullTen,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAffordTen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pull x10", fontWeight = FontWeight.Bold)
                Text("${GACHA_TEN_PULL_COST} berries", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun RarityOddsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (tier in GachaRarity.entries) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(tier.colorHex).copy(alpha = 0.16f),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tier.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(tier.colorHex),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatOdds(tier.weight)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Smoke
                    )
                }
            }
        }
    }
}

private fun formatOdds(weight: Double): String {
    val pct = weight * 100
    return if (pct == pct.toInt().toDouble()) pct.toInt().toString() else String.format(Locale.US, "%.2f", pct)
}

@Composable
private fun CollectionGrid(roster: List<GachaCharacter>, owned: Map<Int, Int>) {
    val sorted = remember(roster) {
        roster.sortedWith(compareBy({ tierSortOrder(it.tier) }, { -it.favourites }))
    }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Collection \u00b7 ${owned.size}/${roster.size} discovered",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Bone,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(sorted, key = { it.id }) { character ->
                CollectionTile(character = character, ownedCount = owned[character.id] ?: 0)
            }
        }
    }
}

private fun tierSortOrder(tier: GachaRarity): Int = when (tier) {
    GachaRarity.MYTHIC -> 0
    GachaRarity.LEGENDARY -> 1
    GachaRarity.EPIC -> 2
    GachaRarity.RARE -> 3
    GachaRarity.COMMON -> 4
}

@Composable
private fun CollectionTile(character: GachaCharacter, ownedCount: Int) {
    val isOwned = ownedCount > 0
    val tierColor = Color(character.tier.colorHex)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .border(2.dp, tierColor.copy(alpha = if (isOwned) 0.9f else 0.3f), RoundedCornerShape(10.dp))
        ) {
            if (isOwned && character.imageUrl != null) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
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
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Smoke, modifier = Modifier.size(22.dp))
                }
            }

            if (isOwned && ownedCount > 1) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "x$ownedCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bone,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isOwned) character.name else "???",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            fontWeight = FontWeight.Medium,
            color = if (isOwned) Bone else Smoke,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Full-screen reveal for a pull (1 or 10 results). Cards fan out one at a
 * time; tapping anywhere advances, and a final tap on the last card
 * dismisses. The rarest pull in the batch gets its name shown larger.
 */
@Composable
private fun PullRevealDialog(pulled: List<GachaCharacter>, onDismiss: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val current = pulled.getOrNull(index)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable {
                if (index < pulled.lastIndex) index++ else onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        if (current != null) {
            val tierColor = Color(current.tier.colorHex)

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.7f) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (current.tier == GachaRarity.MYTHIC || current.tier == GachaRarity.LEGENDARY) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(3.dp, tierColor, RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (current.imageUrl != null) {
                            AsyncImage(
                                model = current.imageUrl,
                                contentDescription = current.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tierColor,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = current.tier.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = current.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Bone,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (index < pulled.lastIndex) "${index + 1} / ${pulled.size} \u00b7 tap to continue" else "tap to close",
                        style = MaterialTheme.typography.bodySmall,
                        color = Smoke
                    )
                }
            }
        }
    }
}
