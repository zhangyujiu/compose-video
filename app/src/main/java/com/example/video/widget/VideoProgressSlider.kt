package com.example.video.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun VideoProgressSlider(
    modifier: Modifier = Modifier,
    currentPosition: Long,      // 当前播放进度，单位：毫秒
    duration: Long,             // 总时长，单位：毫秒
    onSeek: (Long) -> Unit,     // 拖动松手后的回调
    thumbRadius: Dp = 6.dp,
    trackHeight: Dp = 3.dp,
    activeColor: Color = Color.Green,
    inactiveColor: Color = Color.Gray
) {
    // 进度值（0f~1f）
    val progress = remember(currentPosition, duration) {
        if (duration == 0L) 0f else currentPosition.toFloat() / duration
    }

    // 拖动过程中使用的值
    var sliderValue by remember { mutableFloatStateOf(progress) }

    // 是否用户在滑动
    var isUserInteracting by remember { mutableStateOf(false) }

    // 当外部进度更新，且用户没在滑动时，自动更新 sliderValue
    LaunchedEffect(currentPosition, duration, isUserInteracting) {
        if (!isUserInteracting) {
            sliderValue = progress
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isUserInteracting = true
                    },
                    onDragEnd = {
                        // 拖动结束，触发 seek
                        onSeek(
                            (sliderValue * duration)
                                .toLong()
                                .coerceIn(0, duration)
                        )
                        isUserInteracting = false
                    },
                    onDragCancel = {
                        isUserInteracting = false
                    }
                ) { change, _ ->
                    val width = size.width
                    val newX = change.position.x.coerceIn(0f, width.toFloat())
                    val newValue = newX / width
                    sliderValue = newValue
                }
            }
            .height(32.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val trackHeightPx = trackHeight.toPx()
            val centerY = size.height / 2

            val activeWidth = sliderValue * size.width

            // inactive
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(0f, centerY - trackHeightPx / 2),
                size = Size(size.width, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            // active
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(0f, centerY - trackHeightPx / 2),
                size = Size(activeWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            // thumb
            val thumbX = activeWidth
            drawCircle(
                color = Color.Red,
                center = Offset(thumbX, centerY),
                radius = thumbRadius.toPx()
            )
        }
    }
}
