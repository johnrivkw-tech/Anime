package com.example.animetracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        NavBarStyle.SOLID, NavBarStyle.GRADIENT, NavBarStyle.GLASS ->
            PillBar(navBarStyle, selectedIndex, onDestinationClick)
        NavBarStyle.FLOATING_DOTS -> FloatingDotsBar(selectedIndex, onDestinationClick)
        NavBarStyle.DOCK -> DockBar(selectedIndex, onDestinationClick)
        NavBarStyle.UNDERLINE -> UnderlineBar(selectedIndex, onDestinationClick)
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
            else -> Modifier.background(surface, barShape)
        }
        val borderColor = when (navBarStyle) {
            NavBarStyle.GRADIENT -> primary.copy(alpha = 0.5f)
            NavBarStyle.GLASS -> primary.copy(alpha = 0.65f)
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
