package com.musicplayer.melodex.data.repository

import com.musicplayer.melodex.data.db.PlayStatDao
import com.musicplayer.melodex.data.db.PlayStatEntity
import com.musicplayer.melodex.data.model.Song
import kotlinx.coroutines.flow.Flow

class StatsRepository(private val dao: PlayStatDao) {

    fun getAllStats(): Flow<List<PlayStatEntity>> = dao.getAllStats()

    fun getTopPlayed(limit: Int = 10): Flow<List<PlayStatEntity>> = dao.getTopPlayed(limit)

    fun getRecentlyPlayed(limit: Int = 10): Flow<List<PlayStatEntity>> = dao.getRecentlyPlayed(limit)

    fun getTotalPlayCount(): Flow<Int> = dao.getTotalPlayCount()

    fun getTotalPlayedMs(): Flow<Long> = dao.getTotalPlayedMs()

    fun getUniquePlayedCount(): Flow<Int> = dao.getUniquePlayedCount()

    /**
     * 记录一次播放：若歌曲不存在则插入，存在则递增播放次数并更新最近播放时间。
     */
    suspend fun recordPlay(song: Song) {
        val existing = dao.getStat(song.id)
        if (existing == null) {
            dao.upsert(
                PlayStatEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    albumArtUri = song.albumArtUri?.toString(),
                    playCount = 1,
                    skipCount = 0,
                    lastPlayedAt = System.currentTimeMillis(),
                    totalPlayedMs = 0L
                )
            )
        } else {
            dao.incrementPlayCount(song.id, System.currentTimeMillis())
            // 补全之前缺失的 albumArtUri
            if (existing.albumArtUri == null && song.albumArtUri != null) {
                dao.updateAlbumArtUri(song.id, song.albumArtUri.toString())
            }
        }
    }

    suspend fun recordSkip(songId: Long) {
        dao.incrementSkipCount(songId)
    }

    suspend fun recordPlayedDuration(songId: Long, deltaMs: Long) {
        if (deltaMs > 0) dao.addPlayedDuration(songId, deltaMs)
    }

    // ── 收藏 ──

    fun getFavorites(): Flow<List<PlayStatEntity>> = dao.getFavorites()

    suspend fun isFavorite(songId: Long): Boolean = dao.isFavorite(songId) ?: false

    suspend fun toggleFavorite(songId: Long): Boolean {
        val current = dao.isFavorite(songId) ?: false
        val newValue = !current
        dao.setFavorite(songId, newValue)
        return newValue
    }

    suspend fun updateAlbumArtUri(songId: Long, albumArtUri: String?) =
        dao.updateAlbumArtUri(songId, albumArtUri)
}
