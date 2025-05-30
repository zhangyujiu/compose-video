package com.example.video.page

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.state.rememberPresentationState
import com.example.video.R
import com.example.video.ext.noRippleClickable
import com.example.video.player.PlayerFactory
import com.example.video.player.PlayerState
import com.example.video.player.PlayerStatus
import com.example.video.player.rememberPlayerStateController
import com.example.video.widget.VideoProgressSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val TAG = "MediaPlayerPage"

@Composable
fun MediaPlayerPage(
    modifier: Modifier = Modifier,
    page: Int,
    videoUrl: String,
    pagerState: PagerState,
    callbackRef: (PlayerState) -> Unit
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    LaunchedEffect(Unit) {
        player = PlayerFactory.initializePlayer(context)
    }

    player?.let {
        MediaPlayerContent(
            modifier = modifier,
            player = it,
            pagerState = pagerState,
            page = page,
            videoUrl = videoUrl,
            callbackRef = callbackRef
        )
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerContent(
    modifier: Modifier = Modifier,
    player: Player,
    pagerState: PagerState,
    page: Int,
    videoUrl: String,
    callbackRef: (PlayerState) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val playerController = rememberPlayerStateController(player = player)
    val presentationState = rememberPresentationState(player)
    val sliderPosition = remember { mutableIntStateOf(0) }
    var showPlayButton by remember { mutableStateOf(false) }
    var progressTimer by remember { mutableStateOf(false) }
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

    DisposableEffect(Unit) {
        onDispose {
            playerController.release()
            progressTimer = false
        }
    }

    LaunchedEffect(progressTimer) {
        launch {
            while (progressTimer) {
                Log.i(
                    TAG,
                    "total: ${playerController.getDuration()}, position: ${playerController.getPosition()}"
                )
                sliderPosition.intValue = playerController.getPosition()
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
    Box(
        modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .noRippleClickable {
                playerController.toggle()
            }
    ) {
        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier
                .size(screenWidth, screenWidth / playerController.ratio)
                .align(Alignment.Center),
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

        VideoProgressSliderContent(playerController, sliderPosition)
    }
}

@Composable
fun BoxScope.VideoProgressSliderContent(
    playerController: PlayerState,
    sliderPosition: MutableState<Int>
) {
    VideoProgressSlider(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        currentPosition = sliderPosition.value.toLong(),
        duration = playerController.getDuration().toLong(),
        onSeek = {
            playerController.seekTo(it.toInt())
            sliderPosition.value = it.toInt()
        }
    )
}