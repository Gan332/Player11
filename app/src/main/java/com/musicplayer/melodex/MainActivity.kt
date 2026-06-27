package com.musicplayer.melodex

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.musicplayer.melodex.data.preference.ThemeMode
import com.musicplayer.melodex.player.PlaybackService
import com.musicplayer.melodex.ui.components.MiniPlayerBar
import com.musicplayer.melodex.ui.navigation.Screen
import com.musicplayer.melodex.ui.screens.LibraryScreen
import com.musicplayer.melodex.ui.screens.PlayerScreen
import com.musicplayer.melodex.ui.screens.PlaylistDetailScreen
import com.musicplayer.melodex.ui.screens.PlaylistScreen
import com.musicplayer.melodex.ui.screens.SettingsScreen
import com.musicplayer.melodex.ui.screens.StatsScreen
import com.musicplayer.melodex.ui.theme.MelodexTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lifecycleScope.launch {
                (application as MelodexApp).musicRepository.loadSongs()
            }
        }
    }

    /** Android 13+ 通知权限请求 */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* best-effort */ }

    /** 文件选择器：导入本地音频文件 */
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            lifecycleScope.launch {
                val count = (application as MelodexApp).musicRepository.importFiles(uris)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MelodexApp

        // Request audio permission
        handleAudioPermission()
        // Request notification permission (Android 13+)
        handleNotificationPermission()
        // Build MediaController for media session connection
        buildMediaController()

        setContent {
            val themePreferences = app.themePreferences

            MelodexTheme(themePreferences = themePreferences) {
                MelodexMainApp(app = app, onImportClick = { launchFilePicker() })
            }
        }
    }

    override fun onDestroy() {
        mediaController?.release()
        super.onDestroy()
    }

    private fun launchFilePicker() {
        importFileLauncher.launch(arrayOf("audio/*"))
    }

    private fun handleAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED -> {
                lifecycleScope.launch {
                    (application as MelodexApp).musicRepository.loadSongs()
                }
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun buildMediaController() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MelodexMainApp(app: MelodexApp, onImportClick: () -> Unit) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Collect app state
    val songs by app.musicRepository.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentSong by app.musicPlayerController.currentSong.collectAsStateWithLifecycle()
    val isPlaying by app.musicPlayerController.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by app.musicPlayerController.currentPosition.collectAsStateWithLifecycle()
    val duration by app.musicPlayerController.duration.collectAsStateWithLifecycle()
    val isShuffled by app.musicPlayerController.isShuffled.collectAsStateWithLifecycle()
    val repeatMode by app.musicPlayerController.repeatMode.collectAsStateWithLifecycle()
    val lyrics by app.musicPlayerController.lyrics.collectAsStateWithLifecycle()
    val isSleepTimerActive by app.musicPlayerController.isSleepTimerActive.collectAsStateWithLifecycle()
    val sleepTimerRemainingMs by app.musicPlayerController.sleepTimerRemainingMs.collectAsStateWithLifecycle()
    val favorites by app.statsRepository.getFavorites().collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIds = remember(favorites) { favorites.map { it.songId }.toSet() }
    val playlists by app.playlistRepository.getAllPlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    val themeMode by app.themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val dynamicColor by app.themePreferences.dynamicColorEnabled.collectAsStateWithLifecycle(initialValue = true)
    val seedColorLong by app.themePreferences.seedColor.collectAsStateWithLifecycle(initialValue = null)

    // Resolve seed color
    val seedColor = remember(seedColorLong) {
        seedColorLong?.let { Color(it) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem("Library", Icons.Default.LibraryMusic, Screen.Library.route),
        BottomNavItem("Player", Icons.Default.MusicNote, Screen.Player.route),
        BottomNavItem("Playlists", Icons.Default.QueueMusic, Screen.Playlists.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player Bar
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    currentSong?.let { song ->
                        MiniPlayerBar(
                            song = song,
                            isPlaying = isPlaying,
                            onPlayPause = { app.musicPlayerController.playPause() },
                            onNext = { app.musicPlayerController.skipToNext() },
                            onClick = {
                                navController.navigate(Screen.Player.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
                // Bottom Navigation
                NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    songs = songs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onSongClick = { song ->
                        app.musicPlayerController.playSong(song, songs)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onImportClick = onImportClick,
                    favoriteIds = favoriteIds,
                    onToggleFavorite = { song ->
                        scope.launch {
                            app.statsRepository.toggleFavorite(song.id)
                        }
                    },
                    playlists = playlists,
                    onAddSongToPlaylist = { playlistId, songId ->
                        scope.launch {
                            app.playlistRepository.addSongToPlaylist(playlistId, songId)
                        }
                    },
                    onCreatePlaylistAndAddSong = { name, songId ->
                        scope.launch {
                            val playlistId = app.playlistRepository.createPlaylist(name)
                            app.playlistRepository.addSongToPlaylist(playlistId, songId)
                        }
                    }
                )
            }

            composable(Screen.Player.route) {
                PlayerScreen(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    isShuffled = isShuffled,
                    repeatMode = repeatMode,
                    lyrics = lyrics,
                    isSleepTimerActive = isSleepTimerActive,
                    sleepTimerRemainingMs = sleepTimerRemainingMs,
                    onStartSleepTimer = { app.musicPlayerController.startSleepTimer(it) },
                    onCancelSleepTimer = { app.musicPlayerController.cancelSleepTimer() },
                    isFavorite = currentSong?.id?.let { it in favoriteIds } == true,
                    onToggleFavorite = {
                        currentSong?.let { song ->
                            scope.launch {
                                app.statsRepository.toggleFavorite(song.id)
                            }
                        }
                    },
                    onPlayPause = { app.musicPlayerController.playPause() },
                    onNext = { app.musicPlayerController.skipToNext() },
                    onPrevious = { app.musicPlayerController.skipToPrevious() },
                    onShuffle = { app.musicPlayerController.toggleShuffle() },
                    onRepeat = { app.musicPlayerController.cycleRepeatMode() },
                    onSeek = { app.musicPlayerController.seekTo(it) },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    themeMode = themeMode,
                    dynamicColorEnabled = dynamicColor,
                    seedColor = seedColor,
                    onThemeModeChange = { mode ->
                        scope.launch {
                            app.themePreferences.setThemeMode(mode)
                        }
                    },
                    onDynamicColorChange = { enabled ->
                        scope.launch {
                            app.themePreferences.setDynamicColorEnabled(enabled)
                        }
                    },
                    onSeedColorChange = { color ->
                        scope.launch {
                            app.themePreferences.setSeedColor(color?.toArgb()?.toUInt()?.toLong())
                        }
                    },
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route)
                    }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(
                    topPlayed = app.statsRepository.getTopPlayed(10),
                    recentlyPlayed = app.statsRepository.getRecentlyPlayed(10),
                    totalPlayCount = app.statsRepository.getTotalPlayCount(),
                    totalPlayedMs = app.statsRepository.getTotalPlayedMs(),
                    uniquePlayedCount = app.statsRepository.getUniquePlayedCount(),
                    onBack = { navController.navigateUp() }
                )
            }

            // ── 歌单列表 ──
            composable(Screen.Playlists.route) {
                PlaylistScreen(
                    playlists = playlists,
                    getPlaylistSongCount = { playlistId ->
                        app.playlistRepository.getPlaylistSongCount(playlistId)
                    },
                    onCreatePlaylist = { name ->
                        scope.launch { app.playlistRepository.createPlaylist(name) }
                    },
                    onPlaylistClick = { playlistId ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                    },
                    onDeletePlaylist = { playlist ->
                        scope.launch { app.playlistRepository.deletePlaylist(playlist) }
                    },
                    onRenamePlaylist = { id, name ->
                        scope.launch { app.playlistRepository.renamePlaylist(id, name) }
                    }
                )
            }

            // ── 歌单详情 ──
            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
                val playlistEntries by app.playlistRepository.getSongsInPlaylist(playlistId)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                // 将 playlistEntries 映射为 Song 对象
                val playlistSongs = remember(playlistEntries, songs) {
                    playlistEntries.mapNotNull { entry ->
                        songs.find { it.id == entry.songId }
                    }
                }

                val playlistName = remember(playlists, playlistId) {
                    playlists.find { it.id == playlistId }?.name ?: "Playlist"
                }

                PlaylistDetailScreen(
                    playlistName = playlistName,
                    songs = playlistSongs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    favoriteIds = favoriteIds,
                    onSongClick = { song ->
                        app.musicPlayerController.playSong(song, playlistSongs)
                        navController.navigate(Screen.Player.route) { launchSingleTop = true }
                    },
                    onPlayAll = {
                        if (playlistSongs.isNotEmpty()) {
                            app.musicPlayerController.setQueue(playlistSongs, 0)
                            app.musicPlayerController.play()
                            navController.navigate(Screen.Player.route) { launchSingleTop = true }
                        }
                    },
                    onShufflePlay = {
                        if (playlistSongs.isNotEmpty()) {
                            val shuffled = playlistSongs.shuffled()
                            app.musicPlayerController.setQueue(shuffled, 0)
                            app.musicPlayerController.play()
                            navController.navigate(Screen.Player.route) { launchSingleTop = true }
                        }
                    },
                    onRemoveSong = { song ->
                        scope.launch {
                            app.playlistRepository.removeSongFromPlaylist(playlistId, song.id)
                        }
                    },
                    onToggleFavorite = { song ->
                        scope.launch { app.statsRepository.toggleFavorite(song.id) }
                    },
                    onRename = { newName ->
                        scope.launch {
                            app.playlistRepository.renamePlaylist(playlistId, newName)
                        }
                    },
                    onDelete = {
                        scope.launch {
                            app.playlistRepository.getAllPlaylists().first().find { it.id == playlistId }?.let {
                                app.playlistRepository.deletePlaylist(it)
                            }
                            navController.navigateUp()
                        }
                    },
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
