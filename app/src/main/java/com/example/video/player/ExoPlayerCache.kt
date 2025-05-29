package com.example.video.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.example.video.Apps

@UnstableApi
object ExoPlayerCache {
    private var simpleCache: SimpleCache? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    fun getSimpleCache(): SimpleCache {
        if (simpleCache == null) {
            simpleCache = SimpleCache(
                Apps.application.cacheDir,
                LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200),
                StandaloneDatabaseProvider(Apps.application)
            )
        }
        return simpleCache!!
    }
}