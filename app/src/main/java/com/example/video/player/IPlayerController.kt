package com.example.video.player

import android.content.Context
import androidx.media3.common.Player

interface IPlayerController {

    fun setDatasource(datasource: String)

    fun play()

    fun pause()

    fun toggle()

    fun reset()

    fun release()

    fun getPlayerStatus(): PlayerStatus

    fun getPosition(): Int

    fun getDuration(): Int

    fun seekTo(progress: Int)
}