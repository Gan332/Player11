package com.musicplayer.melodex.data.repository

import com.musicplayer.melodex.data.db.PlaylistDao
import com.musicplayer.melodex.data.db.PlaylistEntity
import com.musicplayer.melodex.data.db.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val dao: PlaylistDao) {

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long =
        dao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun renamePlaylist(id: Long, newName: String) {
        val playlist = dao.getPlaylist(id) ?: return
        dao.updatePlaylist(playlist.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) = dao.deletePlaylist(playlist)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val songs = dao.getSongsInPlaylistOnce(playlistId)
        val maxPos = songs.maxOfOrNull { it.position } ?: -1
        dao.addSongToPlaylist(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = maxPos + 1
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        dao.removeSongFromPlaylist(playlistId, songId)

    fun getSongsInPlaylist(playlistId: Long): Flow<List<PlaylistSongEntity>> =
        dao.getSongsInPlaylist(playlistId)

    fun getPlaylistSongCount(playlistId: Long): Flow<Int> =
        dao.getPlaylistSongCount(playlistId)

    suspend fun reorderSongs(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val songs = dao.getSongsInPlaylistOnce(playlistId).toMutableList()
        if (fromIndex !in songs.indices || toIndex !in songs.indices) return
        val item = songs.removeAt(fromIndex)
        songs.add(toIndex, item)
        songs.forEachIndexed { index, entity ->
            dao.updateSongPosition(playlistId, entity.songId, index)
        }
    }
}
