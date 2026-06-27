package com.musicplayer.melodex.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歌单实体，记录用户创建的播放列表。
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
