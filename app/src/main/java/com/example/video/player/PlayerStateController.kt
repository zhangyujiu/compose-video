package com.example.video.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.VideoSize

@Composable
fun rememberPlayerStateController(player: Player): PlayerState {
    val playerControllerState = remember(player) { PlayerState(player) }
    LaunchedEffect(player) {
        playerControllerState.observe()
    }
    return playerControllerState
}

class PlayerState(private val player: Player) : IPlayerController {
    private var datasource: String = ""
    var playerState by mutableStateOf<PlayerStatus>(PlayerStatus.IDEL)
        private set
    var ratio by mutableFloatStateOf(1f)
        private set

    fun observe() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    //这是初始状态、播放器停止时以及播放失败时的状态。在此状态下，播放器将仅保留有限的资源。
                    Player.STATE_IDLE -> {
                        playerState = PlayerStatus.IDEL
                    }
                    //播放器无法立即从当前位置开始播放。这主要是因为需要加载更多数据。
                    Player.STATE_BUFFERING -> {
                        playerState = PlayerStatus.PREPARING
                    }
                    //播放器能够从当前位置立即播放。
                    Player.STATE_READY -> {
                        playerState = PlayerStatus.PREPARED
                    }
                    //播放器完整播放了所有媒体.
                    Player.STATE_ENDED -> {
                        playerState = PlayerStatus.LOOP_PLAYING_BACK
                        player.seekTo(0)
                        play()
                    }
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    playerState =
                        if (playerState == PlayerStatus.PREPARING || playerState == PlayerStatus.PENDING_PLAY) {
                            PlayerStatus.PLAYING
                        } else {
                            PlayerStatus.PREPARED
                        }
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                ratio = videoSize.width / videoSize.height.toFloat()
            }
        })
    }

    override fun setDatasource(datasource: String) {
        this.datasource = datasource
        if (datasource.endsWith(".png")) {
            val mediaItem =
                MediaItem.Builder().setUri(datasource).setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
            player.setMediaItem(mediaItem)
        } else {
            val mediaItem = MediaItem.fromUri(datasource)
            player.setMediaItem(mediaItem)
        }

        player.prepare()
        playerState = PlayerStatus.INITIALIZED
    }

    override fun play() {
        player.playWhenReady = true
        player.play()
        playerState = PlayerStatus.PLAYING
    }

    override fun pause() {
        player.pause()
        playerState = PlayerStatus.PAUSED
    }

    override fun toggle() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    override fun reset() {
        player.seekTo(0)
        player.pause()
    }

    override fun release() {
        playerState = PlayerStatus.RELEASED
        player.release()
    }

    override fun getPlayerStatus(): PlayerStatus = playerState

    override fun getPosition(): Int = player.currentPosition.toInt()

    override fun getDuration(): Int = player.duration.toInt()

    override fun seekTo(progress: Int) {
        player.seekTo(progress.toLong())
    }
}