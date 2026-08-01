package com.example.animetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import com.example.animetracker.ui.navigation.PROFILE_ROUTE
import com.example.animetracker.ui.screens.HomeFeedScreen
import com.example.animetracker.ui.screens.HomeScreen
import com.example.animetracker.ui.screens.GamesScreen
import com.example.animetracker.ui.screens.OnePieceGachaScreen
import com.example.animetracker.ui.screens.LightNovelsScreen
import com.example.animetracker.ui.screens.MangaChaptersScreen
import com.example.animetracker.ui.screens.MangaReaderScreen
import com.example.animetracker.ui.screens.ProfileScreen
import com.example.animetracker.ui.screens.ScheduleScreen
import com.example.animetracker.ui.screens.SearchScreen
import com.example.animetracker.ui.screens.SettingsScreen
import com.example.animetracker.ui.screens.ShrineSplashScreen
import com.example.animetracker.ui.screens.SplashQuality
import com.example.animetracker.ui.components.ReiSealWordmark
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.theme.AnimeTrackerTheme
import com.example.animetracker.viewmodel.AnimeViewModel

class MainActivity : ComponentActivity() {
    // Held at the Activity level (rather than fetched via viewModel() inside
    // setContent) so onNewIntent — which fires when the AniList login
    // redirect comes back into this already-running activity — has a
    // reference to hand the redirect Uri to.
    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAniListRedirectIfPresent(intent)
        setContent {
            val themeOption by viewModel.themeOption.collectAsState()

            AnimeTrackerTheme(themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReiApp(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAniListRedirectIfPresent(intent)
    }

    /** If [intent] is the `rei://anilist-auth` OAuth redirect, hands it to the ViewModel to finish login. */
    private fun handleAniListRedirectIfPresent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "rei" && uri.host == "anilist-auth") {
            viewModel.handleAniListAuthRedirect(uri)
        }
    }
}

@Composable
private fun ReiApp(viewModel: AnimeViewModel) {
    val isAppReady by viewModel.isAppReady.collectAsState()

    // Cinematic hand-off from splash to the real app: the outgoing screen
    // fades + settles back slightly while the incoming one fades + eases
    // forward from a hair past 100% scale — a soft "push through" rather
    // than an instant cut, so the reveal we choreographed in SplashScreen
    // resolves smoothly instead of being clipped off.
    AnimatedContent(
        targetState = isAppReady,
        transitionSpec = {
            (fadeIn(tween(520)) + scaleIn(initialScale = 1.04f, animationSpec = tween(520)))
                .togetherWith(fadeOut(tween(380)) + scaleOut(targetScale = 0.97f, animationSpec = tween(380)))
        },
        label = "splash-to-app"
    ) { ready ->
        if (!ready) {
            // TODO: R.drawable.splash_shrine_bg must match whatever you
            // actually named the artwork file you dropped into res/drawable.
            ShrineSplashScreen(
                background = painterResource(R.drawable.splash_shrine_bg),
                quality = SplashQuality.MEDIUM, // drop to LOW on lower-end devices
                logo = { ReiSealWordmark(markSize = 96.dp) }
            )
        } else {
            MainAppContent(viewModel)
        }
    }
}

@Composable
private fun MainAppContent(viewModel: AnimeViewModel) {
    val navController = rememberNavController()

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
                    onGamesClick = { navController.navigate("games") },
                    onProfileClick = { navController.navigate(PROFILE_ROUTE) }
                )
            }
            composable(Destination.MY_LIST.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Destination.SCHEDULE.route) {
                ScheduleScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onSearchClick = { navController.navigate(Destination.SEARCH.route) }
                )
            }
            composable(Destination.SEARCH.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(PROFILE_ROUTE) {
                ProfileScreen(
                    viewModel = viewModel,
                    onAnimeClick = { aniListId -> navController.navigate("details/$aniListId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen(viewModel = viewModel)
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
            composable("games") {
                BackHandler(onBack = backToHome)
                GamesScreen(
                    viewModel = viewModel,
                    onOpenOnePieceGacha = { navController.navigate("op_gacha") }
                )
            }
            composable("op_gacha") {
                BackHandler(onBack = { navController.popBackStack() })
                OnePieceGachaScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
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
