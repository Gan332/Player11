package com.musicplayer.melodex.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayStatDao {

    @Query("SELECT * FROM play_stats ORDER BY playCount DESC, lastPlayedAt DESC")
    fun getAllStats(): Flow<List<PlayStatEntity>>

    @Query("SELECT * FROM play_stats WHERE songId = :songId")
    suspend fun getStat(songId: Long): PlayStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: PlayStatEntity)

    @Query("""
        UPDATE play_stats
        SET playCount = playCount + 1, lastPlayedAt = :timestamp
        WHERE songId = :songId
    """)
    suspend fun incrementPlayCount(songId: Long, timestamp: Long)

    @Query("""
        UPDATE play_stats
        SET skipCount = skipCount + 1
        WHERE songId = :songId
    """)
    suspend fun incrementSkipCount(songId: Long)

    @Query("""
        UPDATE play_stats
        SET totalPlayedMs = totalPlayedMs + :deltaMs
        WHERE songId = :songId
    """)
    suspend fun addPlayedDuration(songId: Long, deltaMs: Long)

    @Query("SELECT COUNT(*) FROM play_stats WHERE playCount > 0")
    fun getUniquePlayedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(playCount), 0) FROM play_stats")
    fun getTotalPlayCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalPlayedMs), 0) FROM play_stats")
    fun getTotalPlayedMs(): Flow<Long>

    @Query("SELECT * FROM play_stats ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<PlayStatEntity>>

    @Query("SELECT * FROM play_stats ORDER BY playCount DESC, lastPlayedAt DESC LIMIT :limit")
    fun getTopPlayed(limit: Int): Flow<List<PlayStatEntity>>
}
