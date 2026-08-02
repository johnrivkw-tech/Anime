package com.example.animetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import kotlin.math.ceil

/**
 * A poster grid that always sizes itself to its natural (wrap) height and
 * pins to the top, scrolling internally once content overflows — the
 * same way Schedule/Settings' [androidx.compose.foundation.lazy.LazyColumn]
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
 *
 * On top of that: when a caller bounds this grid's height (e.g.
 * `Modifier.heightIn(max = availableHeight)`, as both Search and My List
 * do), a short result set — 4 items in a normally-2-column grid — still
 * only fills 2 small rows and leaves the rest of that bounded space
 * looking empty. Rather than guess a fixed "make cards N% bigger" number
 * (which would look right on one screen size and wrong on another), this
 * measures its own real width/height via [BoxWithConstraints] and picks
 * the *fewest* columns (i.e. biggest cards, since width is what drives
 * height here via [posterAspectRatio]) that still lays every item out
 * without needing to scroll. Once there are enough items that even a
 * single-column layout would overflow the available height, it falls
 * back to [columns] as normal — a long list stays a normal dense grid
 * and scrolls, exactly as before.
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
    /** Portrait poster width:height — matches the `aspectRatio(2f / 3f)` both [com.example.animetracker.ui.components.AnimeGridCard] and My List's poster card use for the artwork itself. */
    posterAspectRatio: Float = 2f / 3f,
    /** Space each card takes up *below* the poster image — title, subtitle/progress row, etc. Both existing poster cards land around this same figure; pass a different value if a future card's text block is meaningfully taller or shorter. */
    cardExtraContentHeight: Dp = 64.dp,
    itemContent: @Composable (T) -> Unit
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val layoutDirection = LocalLayoutDirection.current
        val horizontalPadding = contentPadding.calculateStartPadding(layoutDirection) + contentPadding.calculateEndPadding(layoutDirection)
        val verticalPadding = contentPadding.calculateTopPadding() + contentPadding.calculateBottomPadding()

        val effectiveColumns = remember(items.size, maxWidth, maxHeight, columns) {
            pickColumnCount(
                itemCount = items.size,
                maxColumns = columns,
                availableWidth = maxWidth - horizontalPadding,
                availableHeight = if (maxHeight.isSpecified) maxHeight - verticalPadding else maxHeight,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                cardExtraContentHeight = cardExtraContentHeight,
                posterAspectRatio = posterAspectRatio
            )
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items.chunked(effectiveColumns).forEach { rowItems ->
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
                    repeat(effectiveColumns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Picks the smallest column count (1..[maxColumns]) — i.e. the biggest
 * cards — whose full layout still fits within [availableHeight] without
 * scrolling. Falls back to [maxColumns] if even the densest layout
 * overflows (a normal long list), or if [availableHeight] isn't bounded
 * (the grid wasn't given a max height by its caller) — same as the
 * original fixed-2-column behavior in both cases.
 */
private fun pickColumnCount(
    itemCount: Int,
    maxColumns: Int,
    availableWidth: Dp,
    availableHeight: Dp,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    cardExtraContentHeight: Dp,
    posterAspectRatio: Float
): Int {
    if (itemCount <= 0 || maxColumns <= 1 || !availableHeight.isSpecified || !availableWidth.isSpecified ||
        availableHeight == Dp.Infinity || availableWidth == Dp.Infinity
    ) {
        return maxColumns.coerceAtLeast(1)
    }

    for (cols in 1..maxColumns) {
        val cardWidth = (availableWidth - horizontalSpacing * (cols - 1)) / cols
        if (cardWidth <= 0.dp) continue
        val rowHeight = (cardWidth / posterAspectRatio) + cardExtraContentHeight
        val rows = ceil(itemCount.toFloat() / cols).toInt()
        val totalHeight = rowHeight * rows + verticalSpacing * (rows - 1).coerceAtLeast(0)
        if (totalHeight <= availableHeight) {
            return cols
        }
    }
    return maxColumns
}
