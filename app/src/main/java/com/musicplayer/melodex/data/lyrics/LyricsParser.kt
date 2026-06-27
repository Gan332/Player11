package com.musicplayer.melodex.data.lyrics

import com.musicplayer.melodex.data.model.LyricLine

/**
 * LRC 歌词解析器
 *
 * 支持标准 LRC 格式：
 * [mm:ss.xx] 歌词文本
 * [mm:ss] 歌词文本
 *
 * 支持多时间标签（同一行歌词对应多个时间点）
 */
object LyricsParser {

    // 匹配 [mm:ss.xx] 或 [mm:ss] 格式
    private val TIME_TAG_REGEX = Regex("""\[(\d{1,3}):(\d{2})(?:\.(\d{1,3}))?\]""")

    /**
     * 解析 LRC 歌词文本
     * @param lrcContent LRC 原始文本内容
     * @return 按时间升序排列的歌词行列表
     */
    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()

        lrcContent.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val timeMatches = TIME_TAG_REGEX.findAll(trimmed).toList()
            if (timeMatches.isEmpty()) return@forEach

            // 提取纯文本（去掉所有时间标签）
            val text = TIME_TAG_REGEX.replace(trimmed, "").trim()

            timeMatches.forEach { match ->
                val minutes = match.groupValues[1].toIntOrNull() ?: 0
                val seconds = match.groupValues[2].toIntOrNull() ?: 0
                val millisStr = match.groupValues[3]
                val millis = if (millisStr.isNotEmpty()) {
                    // 毫秒部分：如果是2位数字，需要乘以10（如 .50 → 500ms）
                    val ms = millisStr.toIntOrNull() ?: 0
                    if (millisStr.length == 2) ms * 10 else ms
                } else 0

                val timeMs = (minutes * 60 + seconds) * 1000L + millis
                lines.add(LyricLine(timeMs = timeMs, text = text))
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    /**
     * 根据当前播放位置（毫秒）查找当前应当高亮的歌词行索引
     * @param lyrics 歌词列表
     * @param positionMs 当前播放位置
     * @return 当前歌词行索引，未找到返回 -1
     */
    fun findCurrentIndex(lyrics: List<LyricLine>, positionMs: Long): Int {
        if (lyrics.isEmpty()) return -1

        // 找到最后一个 timeMs <= positionMs 的行
        var index = -1
        for (i in lyrics.indices) {
            if (lyrics[i].timeMs <= positionMs) {
                index = i
            } else {
                break
            }
        }
        return index
    }
}