package com.example.animetracker.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.theme.Blaze
import com.example.animetracker.ui.theme.Bone
import com.example.animetracker.ui.theme.Pulse
import com.example.animetracker.ui.theme.Smoke
import com.example.animetracker.viewmodel.AnimeViewModel
import java.util.Locale

/**
 * Landing spot for berries-powered games, reached from the home screen's
 * hamburger menu. Just the One Piece gacha for now, but laid out as a list
 * so more games can slot in later without a redesign.
 */
@Composable
fun GamesScreen(
    viewModel: AnimeViewModel,
    onOpenOnePieceGacha: () -> Unit
) {
    val stats by viewModel.profileStats.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Games",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Bone
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Spend berries you've earned from watching",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Smoke
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Bone, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${String.format(Locale.US, "%,d", stats.berries)} Berries earned",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Bone
                            )
                            Text(
                                text = "50 per episode \u00b7 500 per completed series",
                                style = MaterialTheme.typography.bodySmall,
                                color = Smoke
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenOnePieceGacha)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Blaze, Pulse))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Casino, contentDescription = null, tint = Bone, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "One Piece Gacha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Bone
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pull for Straw Hats, Warlords, Emperors, and rarer secrets. 100 berries a pull.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Smoke
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Smoke)
                    }
                }
            }
        }
    }
}
