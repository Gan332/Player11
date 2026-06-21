package com.musicplayer.melodex.ui.navigation

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object Player : Screen("player")
    data object Settings : Screen("settings")
    data object Stats : Screen("stats")
}
