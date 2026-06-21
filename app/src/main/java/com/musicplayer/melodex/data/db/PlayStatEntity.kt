package com.musicplayer.melodex.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 播放统计实体，按歌曲 ID 记录播放次数、跳过次数、最近播放时间和累计播放时长。
 */
@Entity(tableName = "play_stats")
data class PlayStatEntity(
    @PrimaryKey
    val songId: Long,
    val title: String,
    val artist: String,
    val playCount: Int = 0,
    val skipCount: Int = 0,
    val lastPlayedAt: Long = 0L,
    val totalPlayedMs: Long = 0L
)
