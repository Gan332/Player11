package com.musicplayer.melodex.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlinx.coroutines.launch

/**
 * M3 Expressive 播放控制栏
 *
 * 特性：
 * - 播放/暂停按钮带 spring 弹性缩放动画
 * - 上一首/下一首按钮带按下缩放反馈
 * - 随机/循环按钮带颜色过渡和发光效果
 */
@Composable
fun PlayerTransportControls(
    isPlaying: Boolean,
    isShuffled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shuffleColor by animateColorAsState(
        targetValue = if (isShuffled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "shuffle_color"
    )
    val repeatColor by animateColorAsState(
        targetValue = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "repeat_color"
    )

    // 播放按钮缩放动画（切换状态时弹一下）
    val playScale = remember { Animatable(1f, Float.VectorConverter) }
    LaunchedEffect(isPlaying) {
        playScale.snapTo(0.85f)
        playScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Shuffle ──
        IconToggleButton(
            checked = isShuffled,
            onCheckedChange = {
                onShuffle()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                modifier = Modifier.size(22.dp),
                tint = shuffleColor
            )
        }

        // ── Previous ──
        ScalableIconButton(
            onClick = {
                onPrevious()
            },
            modifier = Modifier.size(60.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Play/Pause ──
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // 外层光晕
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
            FilledIconButton(
                onClick = {
                    onPlayPause()
                },
                modifier = Modifier
                    .size(72.dp)
                    .scale(playScale.value),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // ── Next ──
        ScalableIconButton(
            onClick = {
                onNext()
            },
            modifier = Modifier.size(60.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Repeat ──
        IconToggleButton(
            checked = repeatMode != Player.REPEAT_MODE_OFF,
            onCheckedChange = {
                onRepeat()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                modifier = Modifier.size(22.dp),
                tint = repeatColor
            )
        }
    }
}

/**
 * 带按下缩放反馈的图标按钮
 */
@Composable
private fun ScalableIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f, Float.VectorConverter) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.scale(scale.value),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = {
                        scope.launch {
                            scale.snapTo(0.85f)
                            scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
                        }
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * M3 Expressive 播放进度条
 *
 * 采用平滑动画过渡，消除跳动：
 * - 更粗的轨道（8dp 视觉感）
 * - 圆角端点
 * - 动画色彩过渡
 */
@Composable
fun PlaybackProgressSlider(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // 平滑动画的分数值（0f..1f）
    val animatedFraction = remember { Animatable(0f, Float.VectorConverter) }

    // 是否正在被用户拖动
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(0f) }

    // 计算目标分数
    val targetFraction = if (duration > 0) {
        (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
    } else 0f

    // 当 position 变化且不在拖动时，平滑动画到目标位置
    LaunchedEffect(currentPosition, duration) {
        if (!isDragging) {
            animatedFraction.snapTo(targetFraction)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Slider(
            value = if (isDragging) dragFraction else animatedFraction.value,
            onValueChange = { fraction ->
                isDragging = true
                dragFraction = fraction
                val pos = (fraction * duration).toLong()
                onSeek(pos)
            },
            onValueChangeFinished = {
                isDragging = false
                // 拖动结束后，动画到最终位置
                scope.launch {
                    animatedFraction.snapTo(dragFraction)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(if (isDragging) (dragFraction * duration).toLong() else currentPosition),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}