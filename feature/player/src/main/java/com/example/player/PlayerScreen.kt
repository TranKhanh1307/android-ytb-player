package com.example.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.YoutubePlayerTheme
import com.example.util.checkIfWifiConnected
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "VideoPlayer"

@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    VideoPlayer(modifier = modifier)
}

@Composable
fun VideoPlayer(modifier: Modifier = Modifier) {


    Column(
        modifier = modifier
    ) {
        CustomVidPlayer()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Cyan),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Place the rest UI here", color = Color.Black)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun CustomVidPlayer(modifier: Modifier = Modifier) {
    val videoId = "YkGRfvn3RUU"
    val lifecycleOwner = LocalLifecycleOwner.current
    var playbackPosition by rememberSaveable { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val activity = context as? Activity // only needed for fullscreen support, otherwise remove
    val orientation = remember { mutableIntStateOf(context.resources.configuration.orientation) }
    var customView: View? = null // only needed for fullscreen support, otherwise remove
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        orientation.intValue = configuration.orientation
    }

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val window = (LocalView.current.context as ComponentActivity).window
    SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    val isWifiConnected = remember { mutableStateOf(checkIfWifiConnected(context)) }
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        modifier = modifier
            .aspectRatio(16f / 9f),
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                val iFramePlayerOptions = IFramePlayerOptions.Builder()
                    .controls(1)
                    .fullscreen(1)
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, playbackPosition)

                        if (isWifiConnected.value) {
                            coroutineScope.launch {
                                delay(500)
                                youTubePlayer.loadVideo(videoId, playbackPosition)
                            }
                        }
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        playbackPosition = second
                    }
                }, true, iFramePlayerOptions)

                addFullscreenListener(object : FullscreenListener {

                    override fun onEnterFullscreen(
                        fullscreenView: View,
                        exitFullscreen: () -> Unit
                    ) {
                        Log.i(TAG, "Entered fullscreen mode")
                        val decorView = activity?.window?.decorView as ViewGroup

                        // can remove this if you don't want to auto rotate to landscape when fullscreen is triggered
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                        decorView.addView(
                            fullscreenView,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        customView = fullscreenView
                    }

                    override fun onExitFullscreen() {
                        Log.i(TAG, "Exited fullscreen mode")
                        val decorView = activity?.window?.decorView as? ViewGroup
                        if (customView != null && decorView != null) {

                            // can remove this if you don't want to auto rotate to landscape when fullscreen is triggered
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                            decorView.removeView(customView)
                            customView = null
                        }
                    }
                })
            }
        }
    )
}

//For reference later
@Composable
fun YouTubePlayer(
    modifier: Modifier = Modifier,
    videoId: String
) {
    val context = LocalContext.current // only needed for fullscreen support, otherwise remove
    val activity = context as? Activity // only needed for fullscreen support, otherwise remove
    val lifecycleOwner = LocalLifecycleOwner.current

    var player: YouTubePlayer? by remember { mutableStateOf(null) }
    var customView: View? = null // only needed for fullscreen support, otherwise remove

    AndroidView(
        factory = { ctx ->
            val youTubePlayerView = YouTubePlayerView(ctx).apply {
                enableAutomaticInitialization = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )

                lifecycleOwner.lifecycle.addObserver(this)

                val listener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                    }
                }

                val options: IFramePlayerOptions =
                    IFramePlayerOptions.Builder() //configure options as needed
                        .controls(1)
                        .fullscreen(1)
                        .autoplay(0)
                        .ivLoadPolicy(3)
                        .build()

                initialize(listener, options)

                // can remove this if you don't want fullscreen
                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(
                        fullscreenView: View, exitFullscreen: () -> Unit
                    ) {
                        val decorView = activity?.window?.decorView as ViewGroup

                        // can remove this if you don't want to auto rotate to landscape when fullscreen is triggered
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                        decorView.addView(
                            fullscreenView,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        customView = fullscreenView
                    }

                    override fun onExitFullscreen() {
                        val decorView = activity?.window?.decorView as? ViewGroup
                        if (customView != null && decorView != null) {

                            // can remove this if you don't want to auto rotate to landscape when fullscreen is triggered
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                            decorView.removeView(customView)
                            customView = null
                        }
                    }
                })

            }
            youTubePlayerView
        }, update = {
            //can use "loadVideo" or "cueVideo" based on preference
            player?.loadOrCueVideo(
                lifecycleOwner.lifecycle, videoId, 0f
            )
        }, modifier = modifier.fillMaxSize()
    )
}


@Preview
@Composable
fun PreviewPlayerScreen() {
    YoutubePlayerTheme {
        PlayerScreen()
    }
}