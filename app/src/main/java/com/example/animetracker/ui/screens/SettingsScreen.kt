package com.example.animetracker.ui.screens

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.theme.AppThemeOption
import com.example.animetracker.viewmodel.AnimeViewModel

/** The settings sections shown as rows in the main menu list. */
private enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    APPEARANCE("Appearance", "Theme and accent color", Icons.Filled.Palette),
    CONTENT_FILTERS("Content Filters", "Age and mature content", Icons.Filled.Shield),
    AI_PERSONALITY("AI Personality", "Customize how the AI talks to you", Icons.Filled.SmartToy)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AnimeViewModel) {
    // null = showing the main settings menu list; otherwise the open section.
    var activeSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }

    Scaffold(
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
                if (targetState != null) {
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
                SettingsMenuList(onSectionSelected = { activeSection = it })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    when (section) {
                        SettingsSection.APPEARANCE -> AppearanceTab(viewModel)
                        SettingsSection.CONTENT_FILTERS -> ContentFiltersTab(viewModel)
                        SettingsSection.AI_PERSONALITY -> AiPersonalityTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuList(onSectionSelected: (SettingsSection) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSection.entries.forEachIndexed { index, section ->
                    SettingsMenuRow(section = section, onClick = { onSectionSelected(section) })
                    if (index != SettingsSection.entries.lastIndex) {
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
}

@Composable
private fun SettingsMenuRow(section: SettingsSection, onClick: () -> Unit) {
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
        selectedTheme = selectedTheme,
        onThemeSelected = { viewModel.setTheme(it) }
    )
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
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

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
private fun ThemeGrid(
    selectedTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    val columns = 3
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppThemeOption.entries.toList().chunked(columns).forEach { row ->
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
