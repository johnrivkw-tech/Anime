package com.example.animetracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.animetracker.BuildConfig
import com.example.animetracker.data.MalXmlPort
import com.example.animetracker.ui.components.ReiWordmark
import com.example.animetracker.ui.model.AvatarFrame
import com.example.animetracker.ui.model.AvatarGlowHalo
import com.example.animetracker.ui.model.brush
import com.example.animetracker.ui.model.GachaRarity
import com.example.animetracker.ui.model.NameGradient
import com.example.animetracker.ui.model.rarityForBerriesCost
import com.example.animetracker.ui.model.textStyle
import com.example.animetracker.ui.navigation.Destination
import com.example.animetracker.ui.navigation.NavBarStyle
import com.example.animetracker.ui.model.currentRank
import com.example.animetracker.ui.theme.AppThemeOption
import com.example.animetracker.viewmodel.AnimeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.sp

/** The settings sections shown as rows in the main menu list. */
private enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    APPEARANCE("Appearance", "Theme, accent color, and background", Icons.Filled.Palette),
    HOME_LAYOUT("Home Layout", "Choose which rows show on Home", Icons.Filled.SpaceDashboard),
    BERRIES_SHOP("Berries Shop", "Spend berries on exclusive cosmetics", Icons.Filled.Storefront),
    CONTENT_FILTERS("Content Filters", "Age and mature content", Icons.Filled.Shield),
    NOTIFICATIONS("Notifications", "Reminders and alerts", Icons.Filled.NotificationsActive),
    BEHAVIOR("Playback & Behavior", "Motion, haptics, data usage", Icons.Filled.Tune),
    AI_PERSONALITY("AI Personality", "Customize how the AI talks to you", Icons.Filled.SmartToy),
    MANGA("Manga", "Show AniList manga in search", Icons.Filled.AutoStories),
    ANILIST_SYNC("AniList Sync", "Log in and sync your list", Icons.Filled.Sync),
    DATA_STORAGE("Data & Storage", "Library stats and reset options", Icons.Filled.Storage),
    ABOUT("About Rei", "Version, credits, and sharing", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AnimeViewModel) {
    // null = showing the main settings menu list; otherwise the open section.
    var activeSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    val reduceMotion by viewModel.reduceMotion.collectAsState()

    Scaffold(
        // This Scaffold is nested inside the app-level one in MainActivity,
        // which already pads every screen for the floating bottom nav bar.
        // Scaffold's default contentWindowInsets would reserve the system
        // nav-bar area a *second* time here, which is what was leaving that
        // large empty gap at the bottom of Settings — zeroing it out here
        // lets the single outer reservation be the only one.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(activeSection?.title ?: "Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (activeSection != null) {
                        IconButton(onClick = { activeSection = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = activeSection,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                } else if (targetState != null) {
                    (slideInHorizontally(tween(220)) { it / 4 } + fadeIn(tween(220))) togetherWith
                        fadeOut(tween(150))
                } else {
                    fadeIn(tween(220)) togetherWith
                        (slideOutHorizontally(tween(220)) { it / 4 } + fadeOut(tween(150)))
                }
            },
            label = "settings-navigation",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { section ->
            if (section == null) {
                SettingsMenuList(viewModel = viewModel, onSectionSelected = { activeSection = it })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    when (section) {
                        SettingsSection.APPEARANCE -> AppearanceTab(viewModel)
                        SettingsSection.HOME_LAYOUT -> HomeLayoutTab(viewModel)
                        SettingsSection.BERRIES_SHOP -> BerriesShopTab(viewModel)
                        SettingsSection.CONTENT_FILTERS -> ContentFiltersTab(viewModel)
                        SettingsSection.NOTIFICATIONS -> NotificationsTab(viewModel)
                        SettingsSection.BEHAVIOR -> BehaviorTab(viewModel)
                        SettingsSection.AI_PERSONALITY -> AiPersonalityTab(viewModel)
                        SettingsSection.MANGA -> MangaTab(viewModel)
                        SettingsSection.ANILIST_SYNC -> AniListSyncTab(viewModel)
                        SettingsSection.DATA_STORAGE -> DataStorageTab(viewModel)
                        SettingsSection.ABOUT -> AboutTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuList(viewModel: AnimeViewModel, onSectionSelected: (SettingsSection) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Soft radial glow behind the profile card instead of a flat black
        // backdrop — this is what was making the whole screen read as
        // "plain": every card sat on identical solid black with nothing
        // to anchor the eye at the top.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ProfileHeaderCard(viewModel)

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("Shop")
            SettingsGroupCard(
                sections = listOf(SettingsSection.BERRIES_SHOP),
                onSectionSelected = onSectionSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("Preferences")
            SettingsGroupCard(
                sections = listOf(
                    SettingsSection.APPEARANCE,
                    SettingsSection.HOME_LAYOUT,
                    SettingsSection.CONTENT_FILTERS,
                    SettingsSection.NOTIFICATIONS,
                    SettingsSection.BEHAVIOR,
                    SettingsSection.AI_PERSONALITY,
                    SettingsSection.MANGA
                ),
                onSectionSelected = onSectionSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("App")
            SettingsGroupCard(
                sections = listOf(SettingsSection.ANILIST_SYNC, SettingsSection.DATA_STORAGE, SettingsSection.ABOUT),
                onSectionSelected = onSectionSelected
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Small uppercase section label with a colored accent bar — replaces the
 *  plain gray caption text with something that actually reads as a header. */
@Composable
private fun SectionLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsGroupCard(
    sections: List<SettingsSection>,
    onSectionSelected: (SettingsSection) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column {
            sections.forEachIndexed { index, section ->
                SettingsMenuRow(
                    section = section,
                    // Alternating primary/secondary icon badges instead of
                    // one flat color for every row — a small touch, but it's
                    // what makes the list feel designed instead of generated.
                    accent = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    onClick = { onSectionSelected(section) }
                )
                if (index != sections.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

/** Small card up top summarizing who's using the app: avatar, name, faction rank, join date. */
@Composable
private fun ProfileHeaderCard(viewModel: AnimeViewModel) {
    val avatarPath by viewModel.profileAvatarPath.collectAsState()
    val displayName by viewModel.profileDisplayName.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val stats by viewModel.profileStats.collectAsState()
    val avatarFrame by viewModel.avatarFrame.collectAsState()
    val nameGradient by viewModel.nameGradient.collectAsState()
    val rankTitle = currentRank(faction, stats.completed)?.title ?: "Unranked"
    val frameBrush = avatarFrame.brush()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AvatarGlowHalo(frame = avatarFrame, avatarSize = 64.dp)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .then(
                            if (avatarFrame.glow) {
                                Modifier.border(width = 3.dp, brush = frameBrush, shape = CircleShape)
                            } else {
                                Modifier.border(width = 2.dp, brush = frameBrush, shape = CircleShape)
                            }
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(frameBrush),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarPath != null) {
                        AsyncImage(
                            model = File(avatarPath!!),
                            contentDescription = "Your avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = displayName.ifBlank { "Anime Fan" },
                    style = nameGradient.textStyle(MaterialTheme.typography.titleMedium),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$rankTitle · ${faction.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Member since ${formatJoinedDate(viewModel.profileJoinedAtMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = formatBerries(stats.berries),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Berries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatJoinedDate(millis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(millis))

private fun formatBerries(amount: Long): String = String.format(Locale.US, "%,d", amount)

@Composable
private fun SettingsMenuRow(section: SettingsSection, accent: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = section.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppearanceTab(viewModel: AnimeViewModel) {
    val selectedTheme by viewModel.themeOption.collectAsState()
    val unlockedThemeNames by viewModel.unlockedThemeNames.collectAsState()
    val lockedCount = AppThemeOption.entries.count { it.berriesCost > 0 && !unlockedThemeNames.contains(it.name) }
    val visibleThemes = AppThemeOption.entries.filter { it.berriesCost <= 0 || unlockedThemeNames.contains(it.name) }

    Text(
        text = "Theme",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Pick an accent that fits your vibe. Changes apply instantly across the app.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    ThemeGrid(
        themes = visibleThemes,
        selectedTheme = selectedTheme,
        onThemeSelected = { viewModel.setTheme(it) }
    )

    if (lockedCount > 0) {
        Text(
            text = "$lockedCount more exclusive ${if (lockedCount == 1) "theme" else "themes"} in the Berries Shop",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp)
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    val trueBlack by viewModel.trueBlackBackground.collectAsState()

    Text(
        text = "Background",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "True Black saves battery on OLED screens. Midnight adds a bit of depth behind cards.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    SettingsChoicePicker(
        options = listOf("True Black" to true, "Midnight" to false),
        selected = trueBlack,
        onSelected = { viewModel.setTrueBlackBackground(it) }
    )
}

@Composable
private fun HomeLayoutTab(viewModel: AnimeViewModel) {
    val showNewReleases by viewModel.showNewReleases.collectAsState()
    val showPopularSeason by viewModel.showPopularSeason.collectAsState()
    val showTopRated by viewModel.showTopRated.collectAsState()
    val showTrendingNow by viewModel.showTrendingNow.collectAsState()
    val showRecommended by viewModel.showRecommended.collectAsState()
    val showAiPicks by viewModel.showAiPicks.collectAsState()

    Text(
        text = "Home Layout",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Turn off any row you don't want cluttering your Home feed. " +
            "\"Continue Tracking\" always shows itself when you have something in progress.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "New Releases",
                subtitle = "Fresh premieres from AniList.",
                checked = showNewReleases,
                onCheckedChange = { viewModel.setShowNewReleases(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Popular This Season",
                subtitle = "What's trending among AniList users right now.",
                checked = showPopularSeason,
                onCheckedChange = { viewModel.setShowPopularSeason(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Top Rated",
                subtitle = "The highest-scored shows on AniList.",
                checked = showTopRated,
                onCheckedChange = { viewModel.setShowTopRated(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Trending Now",
                subtitle = "What's spiking in popularity this week.",
                checked = showTrendingNow,
                onCheckedChange = { viewModel.setShowTrendingNow(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Recommended For You",
                subtitle = "AniList's picks based on what you already track.",
                checked = showRecommended,
                onCheckedChange = { viewModel.setShowRecommended(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "AI Picks For You",
                subtitle = "Rei's own recommendations, tuned to your taste.",
                checked = showAiPicks,
                onCheckedChange = { viewModel.setShowAiPicks(it) }
            )
        }
    }
}

@Composable
private fun BerriesShopTab(viewModel: AnimeViewModel) {
    val context = LocalContext.current
    val balance by viewModel.gachaAvailableBerries.collectAsState()
    val unlockedThemeNames by viewModel.unlockedThemeNames.collectAsState()
    val unlockedNavStyleNames by viewModel.unlockedNavStyleNames.collectAsState()
    val selectedNavStyle by viewModel.navBarStyle.collectAsState()
    val selectedTheme by viewModel.themeOption.collectAsState()
    val unlockedAvatarFrameNames by viewModel.unlockedAvatarFrameNames.collectAsState()
    val selectedAvatarFrame by viewModel.avatarFrame.collectAsState()
    val unlockedNameGradientNames by viewModel.unlockedNameGradientNames.collectAsState()
    val selectedNameGradient by viewModel.nameGradient.collectAsState()

    fun isThemeUnlocked(theme: AppThemeOption) = theme.berriesCost <= 0L || unlockedThemeNames.contains(theme.name)
    fun isNavStyleUnlocked(style: NavBarStyle) = style.berriesCost <= 0L || unlockedNavStyleNames.contains(style.name)
    fun isAvatarFrameUnlocked(frame: AvatarFrame) = frame.berriesCost <= 0L || unlockedAvatarFrameNames.contains(frame.name)
    fun isNameGradientUnlocked(gradient: NameGradient) = gradient.berriesCost <= 0L || unlockedNameGradientNames.contains(gradient.name)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.MonetizationOn,
            contentDescription = null,
            tint = Color(0xFFFFC947),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "%,d Berries".format(balance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        text = "Earned from watching episodes and completing shows. Spend them here on exclusive cosmetics, or in Games on gacha pulls.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    Text(
        text = "Nav Bar Styles",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Change the look of the bar at the bottom of the screen.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        NavBarStyle.entries.forEach { style ->
            NavStyleShopCard(
                style = style,
                owned = isNavStyleUnlocked(style),
                selected = style == selectedNavStyle,
                canAfford = balance >= style.berriesCost,
                onSelect = { viewModel.selectNavBarStyle(style) },
                onPurchase = {
                    val ok = viewModel.purchaseNavStyle(style)
                    if (!ok) {
                        Toast.makeText(context, "Not enough berries yet — keep watching!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Avatar Frames",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "A decorative ring around your profile photo, in Settings and on your Profile page.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AvatarFrame.entries.filter { it.berriesCost > 0 }.forEach { frame ->
            AvatarFrameShopCard(
                frame = frame,
                owned = isAvatarFrameUnlocked(frame),
                selected = frame == selectedAvatarFrame,
                canAfford = balance >= frame.berriesCost,
                onSelect = { viewModel.selectAvatarFrame(frame) },
                onPurchase = {
                    val ok = viewModel.purchaseAvatarFrame(frame)
                    if (!ok) {
                        Toast.makeText(context, "Not enough berries yet — keep watching!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Name Gradient",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Recolor your display name wherever it shows. Legendary Blaze adds a moving gold shine and a fire glow.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        NameGradient.entries.filter { it.berriesCost > 0 }.forEach { gradient ->
            NameGradientShopCard(
                gradient = gradient,
                owned = isNameGradientUnlocked(gradient),
                selected = gradient == selectedNameGradient,
                canAfford = balance >= gradient.berriesCost,
                onSelect = { viewModel.selectNameGradient(gradient) },
                onPurchase = {
                    val ok = viewModel.purchaseNameGradient(gradient)
                    if (!ok) {
                        Toast.makeText(context, "Not enough berries yet — keep watching!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = "Exclusive Themes",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Rare accent colors you won't find in the free Appearance picker.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppThemeOption.entries.filter { it.berriesCost > 0 }.forEach { theme ->
            ThemeShopCard(
                theme = theme,
                owned = isThemeUnlocked(theme),
                selected = theme == selectedTheme,
                canAfford = balance >= theme.berriesCost,
                onSelect = { viewModel.setTheme(theme) },
                onPurchase = {
                    val ok = viewModel.purchaseTheme(theme)
                    if (!ok) {
                        Toast.makeText(context, "Not enough berries yet — keep watching!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

/** A small colored label ("RARE", "LEGENDARY", "MYTHIC"...) borrowed from
 *  the gacha's own rarity ladder, so a cosmetic's price tier reads as the
 *  same status signal a big pull already carries. Free items get nothing. */
@Composable
private fun RarityTag(cost: Long) {
    if (cost <= 0L) return
    val rarity = rarityForBerriesCost(cost)
    Text(
        text = rarity.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(rarity.colorHex),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
    )
}

/** Legendary and Mythic cosmetics get a faint glowing card border in their
 *  rarity color, so the flagship items read as special at a glance and
 *  not just via the text tag. Everything below that tier is unadorned. */
@Composable
private fun rarityCardBorder(cost: Long): Modifier {
    val rarity = rarityForBerriesCost(cost)
    return if (rarity == GachaRarity.LEGENDARY || rarity == GachaRarity.MYTHIC) {
        Modifier.border(
            width = if (rarity == GachaRarity.MYTHIC) 1.5.dp else 1.dp,
            color = Color(rarity.colorHex).copy(alpha = if (rarity == GachaRarity.MYTHIC) 0.7f else 0.5f),
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier
    }
}

@Composable
private fun NavStyleShopCard(
    style: NavBarStyle,
    owned: Boolean,
    selected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(rarityCardBorder(style.berriesCost))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Tiny live preview swatch of the bar treatment itself.
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (style) {
                            NavBarStyle.SOLID -> Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
                            )
                            NavBarStyle.GRADIENT -> Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                )
                            )
                            NavBarStyle.GLASS -> Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                )
                            )
                            NavBarStyle.FLOATING_DOTS -> Brush.linearGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )
                            NavBarStyle.DOCK -> Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
                            )
                            NavBarStyle.UNDERLINE -> Brush.linearGradient(
                                listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (style == NavBarStyle.FLOATING_DOTS || style == NavBarStyle.UNDERLINE) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (style == NavBarStyle.SOLID) 0.2f else 0.6f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = style.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                RarityTag(style.berriesCost)
                Text(
                    text = style.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShopActionButton(
                owned = owned,
                selected = selected,
                cost = style.berriesCost,
                canAfford = canAfford,
                onSelect = onSelect,
                onPurchase = onPurchase
            )
        }
    }
}

@Composable
private fun ThemeShopCard(
    theme: AppThemeOption,
    owned: Boolean,
    selected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(rarityCardBorder(theme.berriesCost))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(theme.primary, theme.secondary)))
                    .border(width = 1.dp, color = theme.primary.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = theme.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                RarityTag(theme.berriesCost)
                Text(
                    text = "Exclusive accent + surface palette",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShopActionButton(
                owned = owned,
                selected = selected,
                cost = theme.berriesCost,
                canAfford = canAfford,
                onSelect = onSelect,
                onPurchase = onPurchase
            )
        }
    }
}

@Composable
private fun AvatarFrameShopCard(
    frame: AvatarFrame,
    owned: Boolean,
    selected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(rarityCardBorder(frame.berriesCost))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AvatarGlowHalo(frame = frame, avatarSize = 36.dp)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (frame.glow) 3.dp else 2.dp,
                            brush = frame.brush(),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(frame.brush())
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = frame.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                RarityTag(frame.berriesCost)
                Text(
                    text = "Ring for your profile photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShopActionButton(
                owned = owned,
                selected = selected,
                cost = frame.berriesCost,
                canAfford = canAfford,
                onSelect = onSelect,
                onPurchase = onPurchase
            )
        }
    }
}

@Composable
private fun NameGradientShopCard(
    gradient: NameGradient,
    owned: Boolean,
    selected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(rarityCardBorder(gradient.berriesCost))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Live preview — this renders through the exact same
            // textStyle() the real name uses, shine/glow included, so
            // what's shown here is exactly what you'd get.
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aa",
                    style = gradient.textStyle(MaterialTheme.typography.titleLarge),
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = gradient.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                RarityTag(gradient.berriesCost)
                Text(
                    text = if (gradient.fireGlow) "Animated shine + fire glow" else "Colors your display name",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShopActionButton(
                owned = owned,
                selected = selected,
                cost = gradient.berriesCost,
                canAfford = canAfford,
                onSelect = onSelect,
                onPurchase = onPurchase
            )
        }
    }
}

/** The right-edge control on a shop card: locked+price, unlocked+selectable, or already-selected. */
@Composable
private fun ShopActionButton(
    owned: Boolean,
    selected: Boolean,
    cost: Long,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    when {
        selected -> AssistChip(
            onClick = {},
            enabled = false,
            leadingIcon = {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            label = { Text("Active") }
        )
        owned -> OutlinedButton(onClick = onSelect) {
            Text("Select")
        }
        else -> Button(
            onClick = onPurchase,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("%,d".format(cost))
        }
    }
}

@Composable
private fun ContentFiltersTab(viewModel: AnimeViewModel) {
    val userAge by viewModel.userAge.collectAsState()
    val matureContentEnabled by viewModel.matureContentEnabled.collectAsState()

    Text(
        text = "Age & Maturity",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Tell us your age so we can tailor what shows up in Browse, Search, and Home — just like on Crunchyroll.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    ContentFiltersSection(
        userAge = userAge,
        matureContentEnabled = matureContentEnabled,
        onAgeCommitted = { viewModel.setAge(it) },
        onMatureContentToggled = { viewModel.setMatureContentEnabled(it) }
    )
}

@Composable
private fun NotificationsTab(viewModel: AnimeViewModel) {
    val episodeReminders by viewModel.episodeReminders.collectAsState()
    val newSeasonAlerts by viewModel.newSeasonAlerts.collectAsState()
    val aiPickNudges by viewModel.aiPickNudges.collectAsState()

    Text(
        text = "Stay in the loop",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Choose what Rei should let you know about. You can change these any time.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "New Episode Reminders",
                subtitle = "Get a nudge when a show you're watching drops a new episode.",
                checked = episodeReminders,
                onCheckedChange = { viewModel.setEpisodeReminders(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "New Season Alerts",
                subtitle = "Hear about it when something on your list gets a new season.",
                checked = newSeasonAlerts,
                onCheckedChange = { viewModel.setNewSeasonAlerts(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "AI Pick Nudges",
                subtitle = "Occasional AI-picked recommendations based on your taste.",
                checked = aiPickNudges,
                onCheckedChange = { viewModel.setAiPickNudges(it) }
            )
        }
    }
}

@Composable
private fun BehaviorTab(viewModel: AnimeViewModel) {
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val dataSaver by viewModel.dataSaver.collectAsState()

    Text(
        text = "Playback & Behavior",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Fine-tune how Rei feels to use.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Reduce Motion",
                subtitle = "Use simpler, faster transitions across the app.",
                checked = reduceMotion,
                onCheckedChange = { viewModel.setReduceMotion(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Haptic Feedback",
                subtitle = "Feel a light tap when you flip a switch in Settings.",
                checked = hapticFeedback,
                onCheckedChange = { viewModel.setHapticFeedback(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                viewModel = viewModel,
                title = "Data Saver",
                subtitle = "Favor lower-resolution artwork when loading covers and banners.",
                checked = dataSaver,
                onCheckedChange = { viewModel.setDataSaver(it) }
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    val defaultStartRoute by viewModel.defaultStartRoute.collectAsState()

    Text(
        text = "Default Start Tab",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Which tab Rei opens on when you launch the app.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    SettingsChoicePicker(
        options = listOf(
            "Home" to Destination.HOME.route,
            "Schedule" to Destination.SCHEDULE.route,
            "My List" to Destination.MY_LIST.route
        ),
        selected = defaultStartRoute,
        onSelected = { viewModel.setDefaultStartRoute(it) }
    )
}

private data class PersonalityPreset(val label: String, val prompt: String)

private val personalityPresets = listOf(
    PersonalityPreset(
        "Hype Hero",
        "You're a hype, energetic anime buddy who treats every recommendation like the " +
            "cold open of something legendary. Big enthusiasm, but always useful and specific."
    ),
    PersonalityPreset(
        "Chill Senpai",
        "You're a laid-back, big-sibling type. Calm, thoughtful recommendations with a bit " +
            "of dry humor. Never pushy."
    ),
    PersonalityPreset(
        "Blunt Critic",
        "You give short, honest, no-fluff opinions. If something is mid, say so. Prioritize " +
            "accuracy and taste over hype."
    ),
    PersonalityPreset(
        "Walking Encyclopedia",
        "You're a walking anime encyclopedia. Prioritize lore, studio trivia, and historical " +
            "context alongside your recommendations."
    )
)

@Composable
private fun AiPersonalityTab(viewModel: AnimeViewModel) {
    val savedPersonality by viewModel.aiPersonality.collectAsState()
    var draft by remember { mutableStateOf(savedPersonality) }

    // Keep the draft in sync if the saved value changes elsewhere (e.g. Reset).
    LaunchedEffect(savedPersonality) { draft = savedPersonality }

    val hasChanges = draft.trim() != savedPersonality.trim() && draft.isNotBlank()

    Text(
        text = "AI Personality",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Write your own instructions for how the AI should talk to you — " +
            "its tone, personality, and style. This applies to AI Picks and AI chat.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )

    Text(
        text = "Quick presets",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        personalityPresets.forEach { preset ->
            AssistChip(
                onClick = { draft = preset.prompt },
                label = { Text(preset.label) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }

    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        label = { Text("Personality / system prompt") },
        placeholder = { Text("e.g. \"You're an upbeat, encouraging anime buddy who loves shonen...\"") },
        minLines = 8,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { viewModel.setAiPersonality(draft) },
            enabled = hasChanges
        ) {
            Text("Save")
        }
        OutlinedButton(
            onClick = { viewModel.resetAiPersonality() }
        ) {
            Text("Reset to default")
        }
    }
}

@Composable
private fun MangaTab(viewModel: AnimeViewModel) {
    val showAniListManga by viewModel.showAniListManga.collectAsState()
    val mangaLibrary by viewModel.mangaLibrary.collectAsState()

    Text(
        text = "Manga",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Off by default. Turn this on and manga titles from AniList will " +
            "show up when you manually search on the Search tab — tap a title " +
            "there to save it here.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsSwitchRow(
            viewModel = viewModel,
            title = "Show AniList Manga",
            subtitle = "Include manga titles when searching.",
            checked = showAniListManga,
            onCheckedChange = { viewModel.setShowAniListManga(it) }
        )
    }

    if (showAniListManga) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Manga",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (mangaLibrary.isEmpty()) {
            Text(
                text = "Nothing saved yet — search for a manga title and tap it to add it here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    mangaLibrary.forEachIndexed { index, manga ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.removeMangaFromLibrary(manga) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = manga.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (index != mangaLibrary.lastIndex) SettingsDivider()
                    }
                }
            }
            Text(
                text = "Tap a title to remove it.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AniListSyncTab(viewModel: AnimeViewModel) {
    val connected by viewModel.aniListConnected.collectAsState()
    val username by viewModel.aniListUsername.collectAsState()
    val avatarUrl by viewModel.aniListAvatarUrl.collectAsState()
    val syncing by viewModel.aniListSyncing.collectAsState()
    val lastSyncedAtMillis by viewModel.aniListLastSyncedAtMillis.collectAsState()
    val syncMessage by viewModel.aniListSyncMessage.collectAsState()
    val context = LocalContext.current

    var showDisconnectConfirm by remember { mutableStateOf(false) }

    // Surface login results / sync results as a one-shot toast, then clear
    // the message so rotating the screen doesn't show it again.
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAniListSyncMessage()
        }
    }

    Text(
        text = "AniList Sync",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = if (connected) {
            "Your library stays in sync with your AniList account. Changes you make here " +
                "push to AniList, and Sync Now pulls in anything you changed over there."
        } else {
            "Log in with your AniList account to import your list and keep it in sync as " +
                "you watch."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    if (connected) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "AniList avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = username ?: "Connected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lastSyncedAtMillis?.let { "Last synced ${formatSyncTime(it)}" }
                            ?: "Not synced yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.syncAniListList() },
                enabled = !syncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (syncing) "Syncing…" else "Sync Now")
            }
            OutlinedButton(
                onClick = { showDisconnectConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Disconnect AniList")
            }
        }
    } else {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.buildAniListAuthUrl()))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log in with AniList")
        }
    }

    if (showDisconnectConfirm) {
        ConfirmActionDialog(
            title = "Disconnect AniList?",
            message = "This signs you out and stops syncing. Anime already in your local " +
                "library is kept as-is.",
            confirmLabel = "Disconnect",
            onConfirm = { viewModel.disconnectAniList() },
            onDismiss = { showDisconnectConfirm = false }
        )
    }
}

private fun formatSyncTime(millis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(millis))

@Composable
private fun DataStorageTab(viewModel: AnimeViewModel) {
    val stats by viewModel.profileStats.collectAsState()
    val context = LocalContext.current

    var showClearWatchlistConfirm by remember { mutableStateOf(false) }
    var showClearChatConfirm by remember { mutableStateOf(false) }
    var showEraseAllConfirm by remember { mutableStateOf(false) }

    val importMalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val entries = context.contentResolver.openInputStream(uri)?.use { input ->
                MalXmlPort.parse(input)
            } ?: emptyList()

            if (entries.isEmpty()) {
                Toast.makeText(context, "No anime entries found in that file.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Importing ${entries.size} titles… fetching artwork for new ones, this may take a bit.",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.importMalXml(entries) { added, updated ->
                    Toast.makeText(
                        context,
                        "Imported: $added added, $updated updated.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't read that file. Is it a valid MAL export?", Toast.LENGTH_SHORT).show()
        }
    }

    val exportMalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(viewModel.exportMalXml().toByteArray())
            }
            Toast.makeText(context, "Export saved.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't save the export file.", Toast.LENGTH_SHORT).show()
        }
    }

    Text(
        text = "Your library",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "A quick snapshot of what's stored on this device.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            StatRow(label = "Anime tracked", value = stats.totalAnime.toString())
            SettingsDivider()
            StatRow(label = "Manga tracked", value = stats.mangaCount.toString())
            SettingsDivider()
            StatRow(label = "Light novels tracked", value = stats.lightNovelCount.toString())
            SettingsDivider()
            StatRow(label = "Episodes watched", value = stats.totalEpisodesWatched.toString())
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "MyAnimeList",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Import a MAL export file to merge it into your library, or export your " +
            "library to a MAL-compatible XML file.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { importMalLauncher.launch(arrayOf("text/xml", "application/xml")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from MyAnimeList")
        }
        OutlinedButton(
            onClick = { exportMalLauncher.launch("rei_mal_export.xml") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export to MyAnimeList")
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Reset options",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "These actions can't be undone, so we'll always ask you to confirm first.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { showClearWatchlistConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Watchlist")
        }
        OutlinedButton(
            onClick = { showClearChatConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear AI Chat History")
        }
        Button(
            onClick = { showEraseAllConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Erase All Local Data")
        }
    }

    if (showClearWatchlistConfirm) {
        ConfirmActionDialog(
            title = "Clear watchlist?",
            message = "This removes every anime you're tracking. Manga, light novels, and chat history are untouched.",
            confirmLabel = "Clear",
            onConfirm = { viewModel.clearWatchlist() },
            onDismiss = { showClearWatchlistConfirm = false }
        )
    }
    if (showClearChatConfirm) {
        ConfirmActionDialog(
            title = "Clear AI chat history?",
            message = "This deletes your saved conversation with the AI. This can't be undone.",
            confirmLabel = "Clear",
            onConfirm = { viewModel.clearChat() },
            onDismiss = { showClearChatConfirm = false }
        )
    }
    if (showEraseAllConfirm) {
        ConfirmActionDialog(
            title = "Erase all local data?",
            message = "This wipes your anime, manga, and light novel library plus AI chat history. " +
                "Your theme, profile, and content filter settings are kept. This can't be undone.",
            confirmLabel = "Erase everything",
            onConfirm = { viewModel.clearAllLocalData() },
            onDismiss = { showEraseAllConfirm = false }
        )
    }
}

@Composable
private fun AboutTab() {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        ReiWordmark(fontSize = 30.sp, markSize = 30.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rei is your all-in-one home for tracking anime, manga, and light novels, " +
                    "with AI-assisted picks tailored to your taste.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Anime and manga data powered by AniList and MangaDex. AI features powered by Gemini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Developer",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            DevCreditRow(
                icon = Icons.Filled.Person,
                name = "JROC",
                role = "Developer"
            )
            Spacer(modifier = Modifier.height(10.dp))
            DevCreditRow(
                icon = Icons.Filled.AutoAwesome,
                name = "Claude",
                role = "AI coding assistant by Anthropic"
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out Rei — the app I use to track anime, manga, and light novels!"
                )
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Rei"))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Share Rei")
    }
}

@Composable
private fun DevCreditRow(icon: ImageVector, name: String, role: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    )
}

/** A titled switch row with an optional haptic tick on toggle, reused across several tabs. */
@Composable
private fun SettingsSwitchRow(
    viewModel: AnimeViewModel,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val hapticsEnabled by viewModel.hapticFeedback.collectAsState()
    val haptics = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

/** A row of mutually-exclusive filter chips for small enum-style choices
 *  (background style, default start tab, etc.) — lighter weight than a
 *  full picker dialog for a 2-3 option pick. */
@Composable
private fun <T> SettingsChoicePicker(
    options: List<Pair<String, T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { (label, value) ->
            val isSelected = value == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeGrid(
    themes: List<AppThemeOption>,
    selectedTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    val columns = 3
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themes.chunked(columns).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { theme ->
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeSwatch(
                            theme = theme,
                            isSelected = theme == selectedTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                    }
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppThemeOption, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(theme.primary, theme.secondary)))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(theme.background.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = theme.primary)
                }
            }
        }
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ContentFiltersSection(
    userAge: Int?,
    matureContentEnabled: Boolean,
    onAgeCommitted: (Int?) -> Unit,
    onMatureContentToggled: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var ageText by remember { mutableStateOf(userAge?.toString() ?: "") }

    fun commitAge() {
        val parsed = ageText.toIntOrNull()?.coerceIn(0, 120)
        ageText = parsed?.toString() ?: ""
        onAgeCommitted(parsed)
    }

    val isAdult = (ageText.toIntOrNull() ?: 0) >= 18

    Column {
        OutlinedTextField(
            value = ageText,
            onValueChange = { input ->
                if (input.length <= 3 && input.all { it.isDigit() }) ageText = input
            },
            label = { Text("Your age") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                commitAge()
                focusManager.clearFocus()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> if (!focusState.isFocused) commitAge() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mature Content (18+)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isAdult) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = when {
                            ageText.isBlank() -> "Enter your age above to manage this setting."
                            !isAdult -> "You must be 18 or older to view mature content."
                            matureContentEnabled -> "Mature titles are included across the app."
                            else -> "Mature titles are hidden across the app."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = matureContentEnabled && isAdult,
                    onCheckedChange = onMatureContentToggled,
                    enabled = isAdult,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
