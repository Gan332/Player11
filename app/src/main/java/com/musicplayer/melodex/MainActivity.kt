package com.musicplayer.melodex

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musicplayer.melodex.data.model.Song
import com.musicplayer.melodex.data.preference.ThemeMode
import com.musicplayer.melodex.ui.navigation.Screen
import com.musicplayer.melodex.ui.screens.LibraryScreen
import com.musicplayer.melodex.ui.screens.PlayerScreen
import com.musicplayer.melodex.ui.screens.SettingsScreen
import com.musicplayer.melodex.ui.theme.MelodexTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            (application as MelodexApp).musicRepository.loadSongs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MelodexApp

        // Request audio permission
        handleAudioPermission()

        setContent {
            val themePreferences = app.themePreferences

            MelodexTheme(themePreferences = themePreferences) {
                MelodexMainApp(app = app)
            }
        }
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
                (application as MelodexApp).musicRepository.loadSongs()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MelodexMainApp(app: MelodexApp) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Collect app state
    val songs by app.musicRepository.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentSong by app.musicPlayerController.currentSong.collectAsStateWithLifecycle()
    val isPlaying by app.musicPlayerController.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by app.musicPlayerController.currentPosition.collectAsStateWithLifecycle()
    val duration by app.musicPlayerController.duration.collectAsStateWithLifecycle()
    val isShuffled by app.musicPlayerController.isShuffled.collectAsStateWithLifecycle()
    val repeatMode by app.musicPlayerController.repeatMode.collectAsStateWithLifecycle()
    val themeMode by app.themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val dynamicColor by app.themePreferences.dynamicColorEnabled.collectAsStateWithLifecycle(initialValue = true)
    val seedColorLong by app.themePreferences.seedColor.collectAsStateWithLifecycle(initialValue = null)

    // Resolve seed color
    val seedColor = remember(seedColorLong) {
        seedColorLong?.let { Color(it.toInt()) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem("Library", Icons.Default.LibraryMusic, Screen.Library.route),
        BottomNavItem("Player", Icons.Default.MusicNote, Screen.Player.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    Scaffold(
        bottomBar = {
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
                    onPlayerNavigate = {
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
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
                            app.themePreferences.setSeedColor(color?.toArgb()?.toLong())
                        }
                    }
                )
            }
        }
    }
}
