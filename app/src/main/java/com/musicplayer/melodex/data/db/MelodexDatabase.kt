package com.musicplayer.melodex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayStatEntity::class], version = 1, exportSchema = false)
abstract class MelodexDatabase : RoomDatabase() {
    abstract fun playStatDao(): PlayStatDao

    companion object {
        @Volatile
        private var INSTANCE: MelodexDatabase? = null

        fun getInstance(context: Context): MelodexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MelodexDatabase::class.java,
                    "melodex.db"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
