package com.musicplayer.melodex.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musicplayer.melodex.data.model.LyricLine
import com.musicplayer.melodex.data.model.Song
import com.musicplayer.melodex.ui.components.EmptyLyricsView
import com.musicplayer.melodex.ui.components.LyricsView
import com.musicplayer.melodex.ui.components.PlaybackProgressSlider
import com.musicplayer.melodex.ui.components.PlayerTransportControls
import com.musicplayer.melodex.ui.components.SleepTimerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffled: Boolean,
    repeatMode: Int,
    lyrics: List<LyricLine>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit,
    isSleepTimerActive: Boolean = false,
    sleepTimerRemainingMs: Long = 0L,
    onStartSleepTimer: (Long) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSleepTimer by remember { mutableStateOf(false) }

    if (showSleepTimer) {
        SleepTimerDialog(
            isActive = isSleepTimerActive,
            remainingMs = sleepTimerRemainingMs,
            onStart = onStartSleepTimer,
            onCancel = onCancelSleepTimer,
            onDismiss = { showSleepTimer = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Now Playing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // 睡眠定时器按钮
                    IconButton(onClick = { showSleepTimer = true }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (isSleepTimerActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        if (currentSong == null) {
            EmptyPlayerState(modifier = Modifier.padding(padding))
        } else {
            PlayerContent(
                song = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isShuffled = isShuffled,
                repeatMode = repeatMode,
                lyrics = lyrics,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onShuffle = onShuffle,
                onRepeat = onRepeat,
                onSeek = onSeek,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PlayerContent(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffled: Boolean,
    repeatMode: Int,
    lyrics: List<LyricLine>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 是否显示歌词视图（否则显示专辑封面）
    var showLyrics by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.25f))

        // ── 专辑封面 / 歌词切换区域 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .clip(RoundedCornerShape(28.dp))
                .clickable { showLyrics = !showLyrics }
        ) {
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = {
                    (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.92f)) togetherWith
                        (fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 1.08f))
                },
                label = "art_lyrics_switch"
            ) { showLyricsView ->
                if (showLyricsView) {
                    if (lyrics.isEmpty()) {
                        EmptyLyricsView(modifier = Modifier.fillMaxSize())
                    } else {
                        LyricsView(
                            lyrics = lyrics,
                            currentPositionMs = currentPosition,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    AlbumArt(
                        song = song,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 切换提示按钮（右上角）
            IconButton(
                onClick = { showLyrics = !showLyrics },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (showLyrics) Icons.Default.MusicNote
                    else Icons.Default.QueueMusic,
                    contentDescription = if (showLyrics) "Show album art" else "Show lyrics",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Song Info ──
        AnimatedContent(
            targetState = song.title,
            transitionSpec = {
                fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 } togetherWith
                    fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 4 }
            },
            label = "song_title"
        ) { title ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = song.artist,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(0.25f))

        // ── Progress ──
        PlaybackProgressSlider(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Controls ──
        PlayerTransportControls(
            isPlaying = isPlaying,
            isShuffled = isShuffled,
            repeatMode = repeatMode,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onShuffle = onShuffle,
            onRepeat = onRepeat
        )

        Spacer(modifier = Modifier.weight(0.35f))
    }
}

@Composable
private fun AlbumArt(
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // 渐变光晕
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 专辑封面
        Surface(
            modifier = Modifier.fillMaxSize(0.78f),
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArtUri)
                        .crossfade(600)
                        .build(),
                    contentDescription = "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPlayerState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No song playing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select a song from your library",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}