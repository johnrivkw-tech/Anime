package com.example.animetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.example.animetracker.ui.screens.AiChatScreen
import com.example.animetracker.ui.screens.AnimeDetailsScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.animetracker.ui.navigation.BottomNavBar
import com.example.animetracker.ui.navigation.Destination
import com.example.animetracker.ui.screens.HomeFeedScreen
import com.example.animetracker.ui.screens.HomeScreen
import com.example.animetracker.ui.screens.LightNovelsScreen
import com.example.animetracker.ui.screens.MangaChaptersScreen
import com.example.animetracker.ui.screens.MangaReaderScreen
import com.example.animetracker.ui.screens.ProfileScreen
import com.example.animetracker.ui.screens.ScheduleScreen
import com.example.animetracker.ui.screens.SearchScreen
import com.example.animetracker.ui.screens.SplashScreen
import com.example.animetracker.ui.theme.AnimeTrackerTheme
import com.example.animetracker.viewmodel.AnimeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VizoraApp()
                }
            }
        }
    }
}

@Composable
private fun VizoraApp() {
    val navController = rememberNavController()
    val viewModel: AnimeViewModel = viewModel()
    val isAppReady by viewModel.isAppReady.collectAsState()

    if (!isAppReady) {
        SplashScreen()
        return
    }

    val backToHome: () -> Unit = {
        navController.popBackStack(Destination.HOME.route, false)
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Destination.HOME.route) {
                HomeFeedScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onChatClick = { navController.navigate("ai_chat") },
                    onReadingClick = { navController.navigate("reading") },
                    onSearchClick = { navController.navigate("search") }
                )
            }
            composable(Destination.MY_LIST.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Destination.SCHEDULE.route) {
                ScheduleScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onSearchClick = { navController.navigate("search") }
                )
            }
            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destination.PROFILE.route) {
                ProfileScreen(viewModel = viewModel)
            }
            composable("reading") {
                BackHandler(onBack = backToHome)
                LightNovelsScreen(
                    viewModel = viewModel,
                    onMangaSelected = { navController.navigate("manga_chapters") }
                )
            }
            composable("manga_chapters") {
                BackHandler(onBack = backToHome)
                MangaChaptersScreen(
                    viewModel = viewModel,
                    onChapterClick = { navController.navigate("manga_reader") },
                    onBack = backToHome
                )
            }
            composable("manga_reader") {
                BackHandler(onBack = backToHome)
                MangaReaderScreen(
                    viewModel = viewModel,
                    onBack = backToHome
                )
            }
            composable("ai_chat") {
                AiChatScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") }
                )
            }
            composable(
                route = "details/{aniListId}",
                arguments = listOf(navArgument("aniListId") { type = NavType.IntType })
            ) { backStackEntry ->
                val aniListId = backStackEntry.arguments?.getInt("aniListId") ?: 0
                AnimeDetailsScreen(
                    viewModel = viewModel,
                    aniListId = aniListId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
