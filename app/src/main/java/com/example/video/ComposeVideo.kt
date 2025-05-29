package com.example.video

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.example.video.ext.LifecycleEffect
import com.example.video.ext.noRippleClickable
import com.example.video.player.ExoPlayerCache
import com.example.video.player.PlayerState
import com.example.video.player.PlayerStatus
import com.example.video.player.rememberPlayerStateController
import com.example.video.widget.VideoProgressSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ComposeVideo(
    modifier: Modifier = Modifier,
    page: Int,
    videoUrl: String,
    pagerState: PagerState,
    callbackRef: (PlayerState) -> Unit
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    LaunchedEffect(Unit) {
        //FIXME:会频繁创建Player
        player = initializePlayer(context)
    }
    player?.let {
        MediaPlayerPage(
            modifier = modifier,
            player = it,
            pagerState = pagerState,
            page = page,
            videoUrl = videoUrl,
            callbackRef = callbackRef
        )
    }
}

@OptIn(UnstableApi::class)
private fun initializePlayer(context: Context): Player {
    //设置预加载配置
    //创建带有自定义缓冲策略的LoadControl
    val loadControl = DefaultLoadControl.Builder()
        .setAllocator(DefaultAllocator(true, 16))
        .setBufferDurationsMs(
            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    // Note: This should be a singleton in your app.
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()

    // Configure the DataSource.Factory with the cache and factory for the desired HTTP stack.
    val cacheDataSourceFactory =
        CacheDataSource.Factory()
            .setCache(ExoPlayerCache.getSimpleCache())
            .setUpstreamDataSourceFactory(httpDataSourceFactory)

    // Inject the DefaultDataSource.Factory when creating the player.
    val player = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
        )
        .build()
    player.playWhenReady = false
    player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
    return player
}

const val TAG = "MediaPlayerPage"

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerPage(
    modifier: Modifier = Modifier,
    player: Player,
    pagerState: PagerState,
    page: Int,
    videoUrl: String,
    callbackRef: (PlayerState) -> Unit
) {
    val playerController = rememberPlayerStateController(player = player)
    val presentationState = rememberPresentationState(player)
    var showPlayButton by remember { mutableStateOf(false) }
    var progressTimer by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.settledPage }.collectLatest {
            if (it == page) {
                callbackRef.invoke(playerController)
            }
        }
    }
    LaunchedEffect(playerController) {
        playerController.setDatasource(videoUrl)
    }

    LifecycleEffect(
        onDestroy = {
            progressTimer = false
        }
    )

    LaunchedEffect(progressTimer) {
        launch {
            while (progressTimer) {
                Log.i(
                    TAG,
                    "total: ${playerController.getDuration()}, position: ${playerController.getPosition()}"
                )
                sliderPosition = playerController.getPosition()
                delay(200)
            }
        }
    }

    LaunchedEffect(playerController.playerState) {
        Log.d(TAG, playerController.playerState.toString())
        when (playerController.playerState) {

            PlayerStatus.PAUSED -> {
                showPlayButton = true
                progressTimer = false
            }

            PlayerStatus.PLAYING -> {
                showPlayButton = false
                progressTimer = true
            }

            else -> {}
        }
    }
    Box(modifier.background(color = Color.Black)) {
        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
            modifier = Modifier
                .resizeWithContentScale(
                    ContentScale.None,
                    presentationState.videoSizeDp
                )
                .noRippleClickable {
                    playerController.toggle()
                },
        )

        if (presentationState.coverSurface) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black)
            )
        }

        if (showPlayButton) {
            Image(
                modifier = Modifier
                    .size(65.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = R.drawable.video_pause_icon),
                contentDescription = null
            )
        }

        VideoProgressSlider(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            currentPosition = sliderPosition.toLong(),
            duration = playerController.getDuration().toLong(),
            onSeek = {
                playerController.seekTo(it.toInt())
                sliderPosition = it.toInt()
            }
        )
    }
}