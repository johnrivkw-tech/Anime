package com.example.animetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home),
    SCHEDULE("schedule", "Schedule", Icons.Filled.DateRange),
    MY_LIST("my_list", "My List", Icons.Filled.Bookmark),
    SEARCH("search", "Search", Icons.Filled.Search),
    SETTINGS("settings", "Settings", Icons.Filled.Settings)
}

/** Profile isn't a bottom-nav tab anymore — it's reached via the avatar
 *  button on the home screen's top bar — but it still needs a stable
 *  route string for navigation. */
const val PROFILE_ROUTE = "profile"
