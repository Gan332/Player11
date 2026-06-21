package com.musicplayer.melodex.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.musicplayer.melodex.data.model.Song
import com.musicplayer.melodex.data.repository.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicPlayerController(
    context: Context,
    private val statsRepository: StatsRepository? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var songQueue: List<Song> = emptyList()
    private var currentIndex = -1

    /** 位置更新协程 */
    private var positionJob: Job? = null

    /** 当前歌曲播放起始时间，用于统计播放时长 */
    private var playStartMs: Long = 0L

    /** 上一次记录的歌曲，用于跳过统计 */
    private var lastSongId: Long? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                    playStartMs = System.currentTimeMillis()
                } else {
                    stopPositionUpdates()
                    // 记录已播放时长
                    val played = System.currentTimeMillis() - playStartMs
                    if (played > 0) {
                        _currentSong.value?.let { song ->
                            scope.launch(Dispatchers.IO) {
                                statsRepository?.recordPlayedDuration(song.id, played)
                            }
                        }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                if (index in songQueue.indices) {
                    // 如果是跳过（非自然结束且非首次），记录跳过
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                        _currentSong.value?.let { prev ->
                            if (prev.id != songQueue[index].id) {
                                scope.launch(Dispatchers.IO) {
                                    statsRepository?.recordSkip(prev.id)
                                }
                            }
                        }
                    }

                    _currentSong.value = songQueue[index]
                    currentIndex = index
                    _currentPosition.value = 0L

                    // 记录播放
                    val newSong = songQueue[index]
                    if (newSong.id != lastSongId) {
                        lastSongId = newSong.id
                        scope.launch(Dispatchers.IO) {
                            statsRepository?.recordPlay(newSong)
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0)
                    _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0)
                }
            }
        })
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (true) {
                _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0)
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        songQueue = songs
        currentIndex = startIndex

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _currentPosition.value = position
    }

    fun skipToNext() {
        exoPlayer.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        exoPlayer.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        val newState = !_isShuffled.value
        _isShuffled.value = newState
        exoPlayer.shuffleModeEnabled = newState
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        exoPlayer.repeatMode = next
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOf(song)
        if (index >= 0) {
            setQueue(queue, index)
            play()
        }
    }

    fun isPlayingSong(song: Song): Boolean =
        _currentSong.value?.id == song.id && _isPlaying.value

    fun release() {
        stopPositionUpdates()
        exoPlayer.release()
        scope.cancel()
    }
}
