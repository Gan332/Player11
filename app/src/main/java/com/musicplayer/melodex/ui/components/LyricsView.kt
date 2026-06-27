package com.musicplayer.melodex.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicplayer.melodex.data.model.LyricLine

/**
 * 歌词显示组件
 *
 * 特性：
 * - 当前播放歌词行高亮（加粗、主色调）
 * - 自动滚动到当前歌词行（居中显示）
 * - 平滑动画过渡
 * - 歌词上方/下方渐变遮罩效果
 */
@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // 计算当前歌词行索引
    val currentIndex = remember(lyrics, currentPositionMs) {
        if (lyrics.isEmpty()) -1
        else {
            var idx = -1
            for (i in lyrics.indices) {
                if (lyrics[i].timeMs <= currentPositionMs) idx = i else break
            }
            idx
        }
    }

    // 自动滚动到当前歌词行（居中）
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            // 滚动到当前行，并偏移使其位于视口上 1/3 处
            val viewportHeight = listState.layoutInfo.viewportSize.height
            val scrollOffset = if (viewportHeight > 0) -viewportHeight / 3 else 0
            listState.animateScrollToItem(
                index = currentIndex + 1, // +1 因为顶部有占位 item
                scrollOffset = scrollOffset
            )
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部留白，让歌词从中间开始
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }

            itemsIndexed(lyrics) { index, line ->
                val isCurrent = index == currentIndex
                val isAdjacent = index == currentIndex - 1 || index == currentIndex + 1

                AnimatedContent(
                    targetState = isCurrent,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    },
                    label = "lyric_line_$index"
                ) {
                    Text(
                        text = line.text.ifEmpty { "♪" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (isCurrent) 22.sp else if (isAdjacent) 17.sp else 15.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else if (isAdjacent) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 10.dp)
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // 顶部渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
        )

        // 底部渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
        )
    }
}

/**
 * 无歌词时的占位状态
 */
@Composable
fun EmptyLyricsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No lyrics",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}