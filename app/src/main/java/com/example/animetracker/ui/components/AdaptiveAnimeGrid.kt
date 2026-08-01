package com.example.animetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A 2-column poster grid that behaves like a normal scrolling
 * [LazyVerticalGrid] once there's enough content to fill the screen, but
 * for a short result set (a handful of search hits, a nearly-empty list,
 * etc.) lays the items out as a plain, non-scrolling grid and centers the
 * whole block vertically — instead of pinning it to the top and leaving a
 * dead slab of background below it.
 *
 * [LazyVerticalGrid] can't be shrink-wrapped and centered directly: it
 * always claims the full height its parent offers regardless of how many
 * items it actually has, so wrapping it in a centered Box has no visual
 * effect. Below the threshold we switch to a real non-lazy layout that can
 * be measured at its natural size and centered.
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
    centerThreshold: Int = 4,
    itemContent: @Composable (T) -> Unit
) {
    if (items.size <= centerThreshold) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
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
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items(items, key = key) { item -> itemContent(item) }
        }
    }
}
