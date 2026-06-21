package com.musicplayer.melodex

import android.app.Application
import com.musicplayer.melodex.data.db.MelodexDatabase
import com.musicplayer.melodex.data.preference.ThemePreferences
import com.musicplayer.melodex.data.repository.MusicRepository
import com.musicplayer.melodex.data.repository.StatsRepository
import com.musicplayer.melodex.player.MusicPlayerController

class MelodexApp : Application() {

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var musicPlayerController: MusicPlayerController
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    lateinit var statsRepository: StatsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = MelodexDatabase.getInstance(this)
        statsRepository = StatsRepository(database.playStatDao())
        musicRepository = MusicRepository(this)
        musicPlayerController = MusicPlayerController(this, statsRepository)
        themePreferences = ThemePreferences(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        musicPlayerController.release()
    }
}
