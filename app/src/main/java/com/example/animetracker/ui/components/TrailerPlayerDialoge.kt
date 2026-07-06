package com.example.animetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.animetracker.ui.theme.Void
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Plays a YouTube trailer inline, in a full-width dialog, instead of
 * kicking the user out to the YouTube app/browser. Uses
 * android-youtube-player (a WebView wrapper around YouTube's IFrame Player
 * API) rather than the YouTube Data API — no API key or quota involved,
 * since we already have the video ID from AniList.
 */
@Composable
fun TrailerPlayerDialog(videoId: String, onDismiss: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var playerView: YouTubePlayerView? = null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Void),
            factory = { context ->
                YouTubePlayerView(context).also { view ->
                    playerView = view
                    lifecycleOwner.lifecycle.addObserver(view)
                    view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                }
            }
        )

        DisposableEffect(Unit) {
            onDispose { playerView?.release() }
        }
    }
}
