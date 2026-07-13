package com.example.animetracker.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A fun, purely-derived achievement unlocked by crossing some threshold in
 * [ProfileStats]. Nothing here is persisted — badges are recomputed from
 * the current stats every time, so they can never drift out of sync with
 * the actual library.
 */
data class ProfileBadge(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)
