package com.musicplayer.melodex.data.model

/**
 * 单行歌词数据模型
 * @param timeMs 歌词起始时间（毫秒）
 * @param text 歌词文本
 */
data class LyricLine(
    val timeMs: Long,
    val text: String
)