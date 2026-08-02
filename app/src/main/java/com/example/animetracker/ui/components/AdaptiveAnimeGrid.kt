package com.example.animetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A 2-column poster grid that always sizes itself to its natural (wrap)
 * height and pins to the top, scrolling internally once content overflows
 * — the same way Schedule/Settings' [androidx.compose.foundation.lazy.LazyColumn]
 * reads: content hugs the top, no leftover slab of background below it,
 * regardless of item count.
 *
 * This intentionally does NOT use [androidx.compose.foundation.lazy.grid.LazyVerticalGrid].
 * Lazy grids/columns always claim the full height their parent offers
 * regardless of how many items they actually have — harmless on screens
 * with enough content to fill that height, but it leaves dead black space
 * whenever the item count is small (a filtered search, a mostly-empty
 * list tab, etc). Building this as a plain non-lazy layout means it's
 * always measured at its true content size, so there's never a gap to
 * leave behind. The tradeoff is no view recycling — fine up to the
 * low hundreds of items (posters are small composables and image loads
 * are cached), but this isn't meant for endless/paginated feeds.
 */
@Composable
fun <T> AdaptiveAnimeGrid(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    columns: Int = 2,
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 18.dp,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            itemContent(item)
                        }
                    }
                    // Pad out an incomplete last row so items stay the
                    // same width as a full row instead of stretching.
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
