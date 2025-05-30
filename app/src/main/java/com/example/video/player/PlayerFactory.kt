package com.example.video.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultAllocator

object PlayerFactory {
    @OptIn(UnstableApi::class)
    fun initializePlayer(context: Context): Player {
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
}