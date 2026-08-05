package com.example.animetracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController, navBarStyle: NavBarStyle = NavBarStyle.SOLID) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedIndex = Destination.entries.indexOfFirst { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }.coerceAtLeast(0)

    val onDestinationClick: (Destination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    when (navBarStyle) {
        NavBarStyle.SOLID, NavBarStyle.GRADIENT, NavBarStyle.GLASS, NavBarStyle.OUTLINE ->
            PillBar(navBarStyle, selectedIndex, onDestinationClick)
        NavBarStyle.FLOATING_DOTS -> FloatingDotsBar(selectedIndex, onDestinationClick)
        NavBarStyle.DOCK -> DockBar(selectedIndex, onDestinationClick)
        NavBarStyle.UNDERLINE -> UnderlineBar(selectedIndex, onDestinationClick)
        NavBarStyle.SEGMENTED -> SegmentedBar(selectedIndex, onDestinationClick)
        NavBarStyle.NOTCH -> NotchBar(selectedIndex, onDestinationClick)
        NavBarStyle.BUBBLE_POP -> BubblePopBar(selectedIndex, onDestinationClick)
        NavBarStyle.ISLANDS -> IslandsBar(selectedIndex, onDestinationClick)
        NavBarStyle.AURORA_DRIFT -> AuroraDriftBar(selectedIndex, onDestinationClick)
        NavBarStyle.VOID_RIFT -> VoidRiftBar(selectedIndex, onDestinationClick)
    }
}

/**
 * The long-standing default layout: one continuous rounded pill bar,
 * floating with margins, that the selected item's background slides
 * around inside of. Solid/Gradient/Glass only change the bar's fill —
 * the shape and behavior are identical.
 */
@Composable
private fun PillBar(
    navBarStyle: NavBarStyle,
    selectedIndex: Int,
    onClick: (Destination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        val barShape = RoundedCornerShape(32.dp)
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val surface = MaterialTheme.colorScheme.surface

        val barBackground: Modifier = when (navBarStyle) {
            NavBarStyle.GRADIENT -> Modifier.background(
                Brush.horizontalGradient(
                    listOf(primary.copy(alpha = 0.30f), secondary.copy(alpha = 0.30f))
                ),
                barShape
            ).background(surface.copy(alpha = 0.55f), barShape)
            NavBarStyle.GLASS -> Modifier.background(surface.copy(alpha = 0.45f), barShape)
            // No fill at all — the screen content shows straight through
            // the pill, leaving just the border to define its shape.
            NavBarStyle.OUTLINE -> Modifier
            else -> Modifier.background(surface, barShape)
        }
        val borderColor = when (navBarStyle) {
            NavBarStyle.GRADIENT -> primary.copy(alpha = 0.5f)
            NavBarStyle.GLASS -> primary.copy(alpha = 0.65f)
            NavBarStyle.OUTLINE -> primary.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(barShape)
                .then(barBackground)
                .border(width = 1.dp, color = borderColor, shape = barShape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                NavPillItem(
                    destination = destination,
                    selected = index == selectedIndex,
                    onClick = { onClick(destination) }
                )
            }
        }
    }
}

/**
 * No continuous bar at all — each destination is its own small floating
 * capsule with real gaps between them. Unselected items are bare icons;
 * the selected item alone gets a filled, labeled capsule. Visually the
 * opposite of the pill bar: separated islands instead of one strip.
 */
@Composable
private fun FloatingDotsBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Destination.entries.forEachIndexed { index, destination ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                animationSpec = tween(220),
                label = "dotBackground"
            )
            val contentColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(220),
                label = "dotContent"
            )
            val height by animateDpAsState(
                targetValue = if (selected) 56.dp else 48.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "dotHeight"
            )

            Row(
                modifier = Modifier
                    .height(height)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = if (selected) 0f else 0.35f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick(destination) }
                    )
                    .padding(horizontal = if (selected) 16.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(tween(180)) + expandHorizontally(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                    ),
                    exit = fadeOut(tween(120)) + shrinkHorizontally(tween(180))
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = destination.label,
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * A classic flush-mounted dock: square top corners, full device width, no
 * side margins or floating gap, sitting directly on the bottom edge with
 * just a hairline top border. The opposite end of the spectrum from the
 * floating pill — this one looks anchored to the chrome, not hovering
 * over the content.
 */
@Composable
private fun DockBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(220),
                    label = "dockTint"
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onClick(destination) }
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = destination.label,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * The most stripped-down layout: no bar, no pill, no fill of any kind —
 * just bare icons in a row over the content, with a short animated
 * underline sliding to sit beneath whichever one is selected.
 */
@Composable
private fun UnderlineBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(220),
                    label = "underlineTint"
                )
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onClick(destination) }
                        )
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (selected) 18.dp else 0.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single solid bar split into equal-width segments by thin dividers,
 * each segment filling with a soft tint when its destination is selected —
 * reads like a tab strip rather than a floating pill.
 */
@Composable
private fun SegmentedBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        val barShape = RoundedCornerShape(18.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = barShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                val bg by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "segmentBg"
                )
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(220),
                    label = "segmentTint"
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(bg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onClick(destination) }
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = destination.label,
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (index != Destination.entries.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

/**
 * The selected destination pops up out of the bar into its own raised,
 * shadowed bubble — a cutout-FAB look, without needing an actual Path
 * cutout in the bar itself; the bubble simply overlaps the bar's top edge.
 */
@Composable
private fun NotchBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 4.dp)
    ) {
        val barShape = RoundedCornerShape(30.dp)
        val primary = MaterialTheme.colorScheme.primary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .align(Alignment.BottomCenter)
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape = barShape),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-22).dp)
                                .size(52.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    ambientColor = primary,
                                    spotColor = primary
                                )
                                .clip(CircleShape)
                                .background(primary)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onClick(destination) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onClick(destination) }
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Each icon is its own free-floating circle with real gaps between them
 * (no connecting bar at all); the selected bubble grows, lifts above the
 * row, and casts a soft colored shadow — a bouncier cousin of Floating Icons.
 */
@Composable
private fun BubblePopBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Destination.entries.forEachIndexed { index, destination ->
            val selected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "bubbleScale"
            )
            val lift by animateDpAsState(
                targetValue = if (selected) 10.dp else 0.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "bubbleLift"
            )
            val bg by animateColorAsState(
                targetValue = if (selected) primary else MaterialTheme.colorScheme.surface,
                animationSpec = tween(220),
                label = "bubbleBg"
            )
            val tint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = "bubbleTint"
            )
            Box(
                modifier = Modifier
                    .offset(y = -lift)
                    .scale(scale)
                    .size(46.dp)
                    .then(
                        if (selected) {
                            Modifier.shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = primary,
                                spotColor = primary
                            )
                        } else Modifier
                    )
                    .clip(CircleShape)
                    .background(bg)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = if (selected) 0f else 0.3f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick(destination) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * The bar splits into two separate floating capsules with a real gap
 * between them — five destinations divide 3-and-2 — the opposite idea from
 * every other style, which keeps one continuous strip.
 */
@Composable
private fun IslandsBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    val destinations = Destination.entries
    val splitIndex = (destinations.size + 1) / 2
    val left = destinations.subList(0, splitIndex)
    val right = destinations.subList(splitIndex, destinations.size)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IslandGroup(
            items = left,
            selectedIndex = selectedIndex,
            globalOffset = 0,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
        IslandGroup(
            items = right,
            selectedIndex = selectedIndex,
            globalOffset = splitIndex,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IslandGroup(
    items: List<Destination>,
    selectedIndex: Int,
    globalOffset: Int,
    onClick: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape = shape),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { localIndex, destination ->
            val selected = (globalOffset + localIndex) == selectedIndex
            val tint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = "islandTint"
            )
            Box(
                modifier = Modifier
                    .size(if (selected) 40.dp else 34.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick(destination) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Mythic. A slow-drifting color wash sweeps across the pill (the same
 * moving-gradient technique [com.example.animetracker.ui.model.NameGradient.LegendaryBlaze]
 * uses for its shine sweep) under a breathing colored glow — this is meant
 * to read as noticeably more alive than any of the free/mid-tier styles.
 */
@Composable
private fun AuroraDriftBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    val transition = rememberInfiniteTransition(label = "auroraDrift")
    val sweep by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraSweep"
    )
    val glowElevation by transition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraGlow"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        val barShape = RoundedCornerShape(32.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = glowElevation.dp,
                    shape = barShape,
                    ambientColor = primary,
                    spotColor = secondary
                )
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.45f),
                            secondary.copy(alpha = 0.45f),
                            primary.copy(alpha = 0.45f)
                        ),
                        start = Offset(sweep, 0f),
                        end = Offset(sweep + 260f, 60f),
                        tileMode = TileMode.Mirror
                    ),
                    barShape
                )
                .border(width = 1.dp, color = primary.copy(alpha = 0.5f), shape = barShape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                NavPillItem(
                    destination = destination,
                    selected = index == selectedIndex,
                    onClick = { onClick(destination) }
                )
            }
        }
    }
}

/**
 * Mythic. A near-black glass bar with a rim that continuously cycles
 * between the theme's two accent colors, and a pulsing radial halo behind
 * whichever icon is selected — the "rift" the style is named for.
 */
@Composable
private fun VoidRiftBar(selectedIndex: Int, onClick: (Destination) -> Unit) {
    val transition = rememberInfiniteTransition(label = "voidRift")
    val ringAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voidRingAlpha"
    )
    val rimPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voidRimPhase"
    )
    val rimColor = lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, rimPhase)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 4.dp)
    ) {
        val barShape = RoundedCornerShape(32.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(barShape)
                .background(Color(0xFF0A0912).copy(alpha = 0.92f))
                .border(width = 1.2.dp, color = rimColor.copy(alpha = 0.75f), shape = barShape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(rimColor.copy(alpha = ringAlpha), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                    }
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onClick(destination) }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavPillItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(durationMillis = 280),
        label = "navPillBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 280),
        label = "navPillContent"
    )
    // Weight isn't natively animatable, so we tween the target value
    // ourselves — this is what turns the pill's width change into a
    // gliding motion instead of an instant snap.
    val pillWeight by animateFloatAsState(
        targetValue = if (selected) 1.7f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navPillWeight"
    )

    Row(
        modifier = Modifier
            .weight(pillWeight)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                animationSpec = tween(220)
            )
        ) {
            Row {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = destination.label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}
