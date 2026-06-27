package com.musicplayer.melodex.ui.navigation

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Player : Screen("player")
    data object Settings : Screen("settings")
    data object Stats : Screen("stats")
    data object Playlists : Screen("playlists")
    data object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}
