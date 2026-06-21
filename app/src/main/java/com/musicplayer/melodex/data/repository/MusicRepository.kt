package com.musicplayer.melodex.data.repository

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musicplayer.melodex.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.importDataStore: DataStore<Preferences> by preferencesDataStore(name = "imported_songs")

class MusicRepository(private val context: Context) {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: Flow<List<Song>> = _songs.asStateFlow()

    private val _importedSongs = MutableStateFlow<List<Song>>(emptyList())
    val importedSongs: Flow<List<Song>> = _importedSongs.asStateFlow()

    private val importedUriKey = stringSetPreferencesKey("imported_uris")

    /**
     * 从 MediaStore 加载设备上的音乐，并合并已导入的本地文件。
     */
    suspend fun loadSongs() = withContext(Dispatchers.IO) {
        val mediaStoreSongs = queryMediaStore()
        val imported = loadImportedSongs()
        _importedSongs.value = imported
        _songs.value = (mediaStoreSongs + imported).distinctBy { it.id }
    }

    private fun queryMediaStore(): List<Song> {
        val songList = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver

        val collection = if (android.os.Build.VERSION.SDK_INT >= 29) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn) ?: continue
                val albumId = cursor.getLong(albumIdColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateColumn)

                val songUri = Uri.parse(data)
                val albumArtUri = if (albumId > 0) {
                    Uri.parse("content://media/external/audio/albumart/$albumId")
                } else null

                songList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = songUri,
                        albumArtUri = albumArtUri,
                        size = size,
                        dateAdded = dateAdded
                    )
                )
            }
        }
        return songList
    }

    /**
     * 从文件选择器导入一个或多个音频文件。
     * 获取持久化 URI 权限，提取元数据，存入 DataStore 并刷新歌曲列表。
     * @return 成功导入的歌曲数量
     */
    suspend fun importFiles(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val newSongs = mutableListOf<Song>()
        for (uri in uris) {
            // 获取持久化读取权限
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // 某些 URI 不支持持久化权限，继续尝试读取
            }

            val song = buildSongFromUri(uri) ?: continue
            newSongs.add(song)
        }

        if (newSongs.isEmpty()) return@withContext 0

        // 持久化 URI 列表
        context.importDataStore.edit { prefs ->
            val existing = prefs[importedUriKey] ?: emptySet()
            prefs[importedUriKey] = existing + newSongs.map { it.uri.toString() }.toSet()
        }

        // 刷新列表
        val current = _importedSongs.value.toMutableList()
        current.addAll(newSongs)
        _importedSongs.value = current.distinctBy { it.uri.toString() }

        val mediaStoreSongs = _songs.value.filter { it.id >= 0 }
        _songs.value = (mediaStoreSongs + _importedSongs.value).distinctBy { it.id }

        newSongs.size
    }

    /**
     * 移除一首已导入的歌曲。
     */
    suspend fun removeImportedSong(song: Song) = withContext(Dispatchers.IO) {
        val uriStr = song.uri.toString()
        context.importDataStore.edit { prefs ->
            val existing = prefs[importedUriKey] ?: return@edit
            prefs[importedUriKey] = existing - uriStr
        }

        // 释放持久化权限
        try {
            context.contentResolver.releasePersistableUriPermission(
                song.uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        val current = _importedSongs.value.toMutableList()
        current.removeAll { it.uri.toString() == uriStr }
        _importedSongs.value = current

        val mediaStoreSongs = _songs.value.filter { it.id >= 0 }
        _songs.value = (mediaStoreSongs + _importedSongs.value).distinctBy { it.id }
    }

    /**
     * 从 DataStore 加载已导入的歌曲。
     */
    private suspend fun loadImportedSongs(): List<Song> = withContext(Dispatchers.IO) {
        val prefs = context.importDataStore.data.first()
        val uriStrings = prefs[importedUriKey] ?: return@withContext emptyList()
        uriStrings.mapNotNull { uriStr ->
            buildSongFromUri(Uri.parse(uriStr))
        }.distinctBy { it.uri.toString() }
    }

    /**
     * 从 URI 构建 Song 对象，使用 MediaMetadataRetriever 提取元数据。
     * 导入的歌曲使用负 ID（基于 URI hashCode 取负）以避免与 MediaStore ID 冲突。
     */
    private fun buildSongFromUri(uri: Uri): Song? {
        return try {
            val resolver = context.contentResolver

            // 文件名
            var displayName: String? = null
            var size = 0L
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
                    if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                }
            }

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)

            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: displayName?.substringBeforeLast('.')
                ?: "Unknown"
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Unknown Album"
            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            mmr.release()

            // 使用负 hashCode 作为 ID，避免与 MediaStore 正 ID 冲突
            val songId = -(uri.toString().hashCode().toLong().absoluteValue)

            Song(
                id = songId,
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                uri = uri,
                albumArtUri = null,
                size = size,
                dateAdded = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }

    private val Long.absoluteValue: Long get() = if (this < 0) -this else this
}
