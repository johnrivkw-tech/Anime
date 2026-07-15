package com.example.animetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home),
    SCHEDULE("schedule", "Schedule", Icons.Filled.DateRange),
    MY_LIST("my_list", "My List", Icons.Filled.List),
    PROFILE("profile", "Profile", Icons.Filled.Person),
    SETTINGS("settings", "Settings", Icons.Filled.Settings)
}
