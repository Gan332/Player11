import 'package:flutter/material.dart';
import '../models/song.dart';
import '../services/database_service.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

enum SortOption {
  titleAsc,
  titleDesc,
  artistAsc,
  shortest,
  longest,
  newest,
  oldest,
}

class LibraryProvider extends ChangeNotifier {
  List<Song> _songs = [];
  List<Song> _filteredSongs = [];
  String _searchQuery = '';
  String _searchField = 'all';
  SortOption _sortOption = SortOption.titleAsc;
  List<int> _favoriteIds = [];
  Set<int> _importedSongIds = {};

  List<Song> get songs => _filteredSongs;
  List<int> get favoriteIds => _favoriteIds;
  SortOption get sortOption => _sortOption;
  String get searchQuery => _searchQuery;

  LibraryProvider() {
    _loadImportedSongs();
  }

  Future<void> _loadImportedSongs() async {
    final prefs = await SharedPreferences.getInstance();
    final ids = prefs.getStringList('importedSongIds') ?? [];
    _importedSongIds = ids.map((e) => int.parse(e)).toSet();
  }

  Future<void> _saveImportedSongs() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(
      'importedSongIds',
      _importedSongIds.map((e) => e.toString()).toList(),
    );
  }

  Future<void> loadSongs() async {
    _songs = await _scanDeviceSongs();
    _songs.addAll(await _loadImportedSongFiles());
    _favoriteIds = await DatabaseService.getFavoriteIds();
    _applyFiltersAndSort();
    notifyListeners();
  }

  Future<List<Song>> _scanDeviceSongs() async {
    // This would use platform channels to query MediaStore
    // For now, return empty list - will be implemented with method channel
    return [];
  }

  Future<List<Song>> _loadImportedSongFiles() async {
    // Load imported songs from stored URIs
    return [];
  }

  void search(String query) {
    _searchQuery = query;
    _applyFiltersAndSort();
    notifyListeners();
  }

  void setSearchField(String field) {
    _searchField = field;
    _applyFiltersAndSort();
    notifyListeners();
  }

  void setSortOption(SortOption option) {
    _sortOption = option;
    _applyFiltersAndSort();
    notifyListeners();
  }

  void _applyFiltersAndSort() {
    var filtered = List<Song>.from(_songs);

    if (_searchQuery.isNotEmpty) {
      final query = _searchQuery.toLowerCase();
      filtered = filtered.where((song) {
        switch (_searchField) {
          case 'title':
            return song.title.toLowerCase().contains(query);
          case 'artist':
            return song.artist.toLowerCase().contains(query);
          case 'album':
            return song.album.toLowerCase().contains(query);
          default:
            return song.title.toLowerCase().contains(query) ||
                song.artist.toLowerCase().contains(query) ||
                song.album.toLowerCase().contains(query);
        }
      }).toList();
    }

    filtered.sort((a, b) {
      switch (_sortOption) {
        case SortOption.titleAsc:
          return a.title.toLowerCase().compareTo(b.title.toLowerCase());
        case SortOption.titleDesc:
          return b.title.toLowerCase().compareTo(a.title.toLowerCase());
        case SortOption.artistAsc:
          return a.artist.toLowerCase().compareTo(b.artist.toLowerCase());
        case SortOption.shortest:
          return a.duration.compareTo(b.duration);
        case SortOption.longest:
          return b.duration.compareTo(a.duration);
        case SortOption.newest:
          return b.dateAdded.compareTo(a.dateAdded);
        case SortOption.oldest:
          return a.dateAdded.compareTo(b.dateAdded);
      }
    });

    _filteredSongs = filtered;
  }

  Future<void> toggleFavorite(int songId) async {
    if (_favoriteIds.contains(songId)) {
      _favoriteIds.remove(songId);
      await DatabaseService.setFavorite(songId, false);
    } else {
      _favoriteIds.add(songId);
      await DatabaseService.setFavorite(songId, true);
    }
    notifyListeners();
  }

  bool isFavorite(int songId) {
    return _favoriteIds.contains(songId);
  }

  Future<void> importSong(Song song) async {
    _songs.add(song);
    _importedSongIds.add(song.id);
    await _saveImportedSongs();
    _applyFiltersAndSort();
    notifyListeners();
  }

  Future<void> removeImportedSong(int songId) async {
    _songs.removeWhere((s) => s.id == songId);
    _importedSongIds.remove(songId);
    await _saveImportedSongs();
    _applyFiltersAndSort();
    notifyListeners();
  }
}
