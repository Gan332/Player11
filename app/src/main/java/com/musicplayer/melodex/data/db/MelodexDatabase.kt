package com.musicplayer.melodex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlayStatEntity::class, PlaylistEntity::class, PlaylistSongEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MelodexDatabase : RoomDatabase() {
    abstract fun playStatDao(): PlayStatDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: MelodexDatabase? = null

        /** v1 → v2：新增 albumArtUri / isFavorite 列 + 歌单表 */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE play_stats ADD COLUMN albumArtUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE play_stats ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "updatedAt INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                        "playlistId INTEGER NOT NULL, " +
                        "songId INTEGER NOT NULL, " +
                        "position INTEGER NOT NULL, " +
                        "addedAt INTEGER NOT NULL, " +
                        "PRIMARY KEY(playlistId, songId), " +
                        "FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_songs_playlistId ON playlist_songs(playlistId)")
            }
        }

        fun getInstance(context: Context): MelodexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MelodexDatabase::class.java,
                    "melodex.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also {
                        INSTANCE = it
                    }
            }
        }
    }
}
