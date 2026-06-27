import 'package:flutter/material.dart';
import '../models/play_stat.dart';
import '../services/database_service.dart';

class StatsProvider extends ChangeNotifier {
  List<PlayStat> _topPlayed = [];
  List<PlayStat> _recentlyPlayed = [];
  int _totalPlays = 0;
  int _uniqueSongs = 0;
  int _totalListenedMs = 0;

  List<PlayStat> get topPlayed => _topPlayed;
  List<PlayStat> get recentlyPlayed => _recentlyPlayed;
  int get totalPlays => _totalPlays;
  int get uniqueSongs => _uniqueSongs;
  int get totalListenedMs => _totalListenedMs;

  String get formattedTotalListened {
    final hours = _totalListenedMs ~/ 3600000;
    final minutes = (_totalListenedMs % 3600000) ~/ 60000;
    if (hours > 0) {
      return '${hours}h ${minutes}m';
    }
    return '${minutes}m';
  }

  Future<void> loadStats() async {
    _topPlayed = await DatabaseService.getTopPlayed(limit: 10);
    _recentlyPlayed = await DatabaseService.getRecentlyPlayed(limit: 10);
    _totalPlays = await DatabaseService.getTotalPlayCount();
    _uniqueSongs = await DatabaseService.getUniquePlayedCount();
    _totalListenedMs = await DatabaseService.getTotalPlayedMs();
    notifyListeners();
  }

  Future<void> recordPlay(Song song) async {
    await DatabaseService.upsertPlayStat(PlayStat(
      songId: song.id,
      title: song.title,
      artist: song.artist,
      albumArtUri: song.albumArtUri,
    ));
    await DatabaseService.incrementPlayCount(song.id);
    await loadStats();
  }

  Future<void> recordSkip(int songId) async {
    await DatabaseService.incrementSkipCount(songId);
  }

  Future<void> addPlayedDuration(int songId, int deltaMs) async {
    await DatabaseService.addPlayedDuration(songId, deltaMs);
  }
}
