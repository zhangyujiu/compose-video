package com.example.video.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.example.video.datas.videos
import com.example.video.ext.LifecycleEffect
import com.example.video.player.PlayerState

@Composable
fun VerticalVideoPager(modifier: Modifier = Modifier) {

    val view = LocalView.current
    val controllerRef = remember { mutableStateOf<PlayerState?>(null) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { videos.size })

    LifecycleEffect(
        onResume = {
            view.keepScreenOn = true
            controllerRef.value?.play()
        },
        onPause = {
            view.keepScreenOn = false
            controllerRef.value?.pause()
        }
    )

    VerticalPager(
        modifier = modifier.fillMaxSize(),
        state = pagerState,
    ) { page ->
        val url = videos[page]
        MediaPlayerPage(
            modifier = Modifier.fillMaxSize(),
            videoUrl = url,
            pagerState = pagerState,
            page = page,
            callbackRef = {
                controllerRef.value?.pause()
                controllerRef.value = it
                controllerRef.value?.play()
            }
        )
    }
}
