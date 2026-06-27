package com.musicplayer.melodex.data.lyrics

import android.content.Context
import android.net.Uri
import com.musicplayer.melodex.data.model.LyricLine
import com.musicplayer.melodex.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 歌词加载器
 *
 * 尝试从与歌曲同目录、同文件名的 .lrc 文件中加载歌词
 * 例如：/Music/song.mp3 → /Music/song.lrc
 */
object LyricsLoader {

    /**
     * 为指定歌曲加载歌词
     * 尝试多种策略查找歌词文件
     */
    suspend fun loadLyrics(context: Context, song: Song): List<LyricLine>? {
        return withContext(Dispatchers.IO) {
            try {
                // 策略1：尝试从歌曲 URI 路径推导 .lrc 文件路径
                val songPath = song.uri.path ?: return@withContext null
                val lrcPath = songPath.replaceAfterLast('.', "lrc")
                val lrcUri = Uri.parse(lrcPath)

                val content = readUriContent(context, lrcUri)
                if (content != null) {
                    return@withContext LyricsParser.parse(content)
                }

                // 策略2：尝试从歌曲 URI 所在目录查找同名 .lrc
                val dirPath = songPath.substringBeforeLast('/')
                val fileName = songPath.substringAfterLast('/').substringBeforeLast('.')
                val lrcPath2 = "$dirPath/$fileName.lrc"
                val lrcUri2 = Uri.parse(lrcPath2)

                val content2 = readUriContent(context, lrcUri2)
                if (content2 != null) {
                    return@withContext LyricsParser.parse(content2)
                }

                null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun readUriContent(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).readText()
            }
        } catch (e: Exception) {
            null
        }
    }
}