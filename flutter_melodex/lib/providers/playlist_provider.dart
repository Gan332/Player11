import 'package:flutter/material.dart';
import '../models/playlist.dart';
import '../models/song.dart';
import '../services/database_service.dart';

class PlaylistProvider extends ChangeNotifier {
  List<Playlist> _playlists = [];
  Map<int, List<Song>> _playlistSongs = {};

  List<Playlist> get playlists => _playlists;

  PlaylistProvider() {
    loadPlaylists();
  }

  Future<void> loadPlaylists() async {
    _playlists = await DatabaseService.getAllPlaylists();
    notifyListeners();
  }

  Future<int> createPlaylist(String name) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final playlist = Playlist(
      name: name,
      createdAt: now,
      updatedAt: now,
    );
    final id = await DatabaseService.insertPlaylist(playlist);
    _playlists.insert(0, playlist.copyWith(id: id));
    notifyListeners();
    return id;
  }

  Future<void> renamePlaylist(int id, String newName) async {
    final playlist = _playlists.firstWhere((p) => p.id == id);
    final updated = playlist.copyWith(
      name: newName,
      updatedAt: DateTime.now().millisecondsSinceEpoch,
    );
    await DatabaseService.updatePlaylist(updated);
    final index = _playlists.indexWhere((p) => p.id == id);
    _playlists[index] = updated;
    notifyListeners();
  }

  Future<void> deletePlaylist(int id) async {
    await DatabaseService.deletePlaylist(id);
    _playlists.removeWhere((p) => p.id == id);
    _playlistSongs.remove(id);
    notifyListeners();
  }

  Future<void> addSongToPlaylist(int playlistId, int songId) async {
    final count = await DatabaseService.getPlaylistSongCount(playlistId);
    final entry = PlaylistSong(
      playlistId: playlistId,
      songId: songId,
      position: count,
      addedAt: DateTime.now().millisecondsSinceEpoch,
    );
    await DatabaseService.addSongToPlaylist(entry);
    _playlistSongs.remove(playlistId);
    notifyListeners();
  }

  Future<void> removeSongFromPlaylist(int playlistId, int songId) async {
    await DatabaseService.removeSongFromPlaylist(playlistId, songId);
    _playlistSongs.remove(playlistId);
    notifyListeners();
  }

  Future<List<int>> getSongIdsInPlaylist(int playlistId) async {
    return await DatabaseService.getSongIdsInPlaylist(playlistId);
  }

  Future<int> getPlaylistSongCount(int playlistId) async {
    return await DatabaseService.getPlaylistSongCount(playlistId);
  }

  Future<void> clearPlaylist(int playlistId) async {
    await DatabaseService.clearPlaylist(playlistId);
    _playlistSongs.remove(playlistId);
    notifyListeners();
  }
}
