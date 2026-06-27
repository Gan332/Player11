import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/play_stat.dart';
import '../models/playlist.dart';

class DatabaseService {
  static Database? _database;
  static const String _dbName = 'melodex.db';
  static const int _dbVersion = 2;

  static Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  static Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, _dbName);

    return await openDatabase(
      path,
      version: _dbVersion,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
    );
  }

  static Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE play_stats (
        songId INTEGER PRIMARY KEY,
        title TEXT NOT NULL,
        artist TEXT NOT NULL,
        albumArtUri TEXT,
        isFavorite INTEGER NOT NULL DEFAULT 0,
        playCount INTEGER NOT NULL DEFAULT 0,
        skipCount INTEGER NOT NULL DEFAULT 0,
        lastPlayedAt INTEGER NOT NULL DEFAULT 0,
        totalPlayedMs INTEGER NOT NULL DEFAULT 0
      )
    ''');

    await db.execute('''
      CREATE TABLE playlists (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        createdAt INTEGER NOT NULL,
        updatedAt INTEGER NOT NULL
      )
    ''');

    await db.execute('''
      CREATE TABLE playlist_songs (
        playlistId INTEGER NOT NULL,
        songId INTEGER NOT NULL,
        position INTEGER NOT NULL,
        addedAt INTEGER NOT NULL,
        PRIMARY KEY (playlistId, songId),
        FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE
      )
    ''');

    await db.execute(
      'CREATE INDEX idx_playlist_songs_playlistId ON playlist_songs(playlistId)',
    );
  }

  static Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 2) {
      await db.execute('ALTER TABLE play_stats ADD COLUMN albumArtUri TEXT');
      await db.execute('ALTER TABLE play_stats ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0');
      await db.execute('''
        CREATE TABLE playlists (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name TEXT NOT NULL,
          createdAt INTEGER NOT NULL,
          updatedAt INTEGER NOT NULL
        )
      ''');
      await db.execute('''
        CREATE TABLE playlist_songs (
          playlistId INTEGER NOT NULL,
          songId INTEGER NOT NULL,
          position INTEGER NOT NULL,
          addedAt INTEGER NOT NULL,
          PRIMARY KEY (playlistId, songId),
          FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE
        )
      ''');
      await db.execute(
        'CREATE INDEX idx_playlist_songs_playlistId ON playlist_songs(playlistId)',
      );
    }
  }

  // PlayStat operations
  static Future<void> upsertPlayStat(PlayStat stat) async {
    final db = await database;
    await db.insert(
      'play_stats',
      stat.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  static Future<PlayStat?> getPlayStat(int songId) async {
    final db = await database;
    final maps = await db.query(
      'play_stats',
      where: 'songId = ?',
      whereArgs: [songId],
    );
    if (maps.isEmpty) return null;
    return PlayStat.fromMap(maps.first);
  }

  static Future<List<PlayStat>> getAllPlayStats() async {
    final db = await database;
    final maps = await db.query('play_stats');
    return maps.map((m) => PlayStat.fromMap(m)).toList();
  }

  static Future<void> incrementPlayCount(int songId) async {
    final db = await database;
    await db.rawUpdate('''
      INSERT INTO play_stats (songId, title, artist, playCount, lastPlayedAt)
      VALUES (?, '', '', 1, ?)
      ON CONFLICT(songId) DO UPDATE SET
        playCount = playCount + 1,
        lastPlayedAt = ?
    ''', [songId, DateTime.now().millisecondsSinceEpoch, DateTime.now().millisecondsSinceEpoch]);
  }

  static Future<void> incrementSkipCount(int songId) async {
    final db = await database;
    await db.rawUpdate('''
      INSERT INTO play_stats (songId, title, artist, skipCount)
      VALUES (?, '', '', 1)
      ON CONFLICT(songId) DO UPDATE SET
        skipCount = skipCount + 1
    ''', [songId]);
  }

  static Future<void> addPlayedDuration(int songId, int deltaMs) async {
    final db = await database;
    await db.rawUpdate('''
      INSERT INTO play_stats (songId, title, artist, totalPlayedMs)
      VALUES (?, '', '', ?)
      ON CONFLICT(songId) DO UPDATE SET
        totalPlayedMs = totalPlayedMs + ?
    ''', [songId, deltaMs, deltaMs]);
  }

  static Future<void> setFavorite(int songId, bool isFavorite) async {
    final db = await database;
    await db.rawUpdate('''
      INSERT INTO play_stats (songId, title, artist, isFavorite)
      VALUES (?, '', '', ?)
      ON CONFLICT(songId) DO UPDATE SET
        isFavorite = ?
    ''', [songId, isFavorite ? 1 : 0, isFavorite ? 1 : 0]);
  }

  static Future<bool> isFavorite(int songId) async {
    final db = await database;
    final maps = await db.query(
      'play_stats',
      columns: ['isFavorite'],
      where: 'songId = ?',
      whereArgs: [songId],
    );
    if (maps.isEmpty) return false;
    return (maps.first['isFavorite'] as int) == 1;
  }

  static Future<List<int>> getFavoriteIds() async {
    final db = await database;
    final maps = await db.query(
      'play_stats',
      columns: ['songId'],
      where: 'isFavorite = 1',
    );
    return maps.map((m) => m['songId'] as int).toList();
  }

  static Future<List<PlayStat>> getTopPlayed({int limit = 10}) async {
    final db = await database;
    final maps = await db.query(
      'play_stats',
      orderBy: 'playCount DESC',
      limit: limit,
    );
    return maps.map((m) => PlayStat.fromMap(m)).toList();
  }

  static Future<List<PlayStat>> getRecentlyPlayed({int limit = 10}) async {
    final db = await database;
    final maps = await db.query(
      'play_stats',
      where: 'lastPlayedAt > 0',
      orderBy: 'lastPlayedAt DESC',
      limit: limit,
    );
    return maps.map((m) => PlayStat.fromMap(m)).toList();
  }

  static Future<int> getUniquePlayedCount() async {
    final db = await database;
    final result = await db.rawQuery('SELECT COUNT(*) as count FROM play_stats WHERE playCount > 0');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  static Future<int> getTotalPlayCount() async {
    final db = await database;
    final result = await db.rawQuery('SELECT SUM(playCount) as total FROM play_stats');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  static Future<int> getTotalPlayedMs() async {
    final db = await database;
    final result = await db.rawQuery('SELECT SUM(totalPlayedMs) as total FROM play_stats');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  // Playlist operations
  static Future<int> insertPlaylist(Playlist playlist) async {
    final db = await database;
    return await db.insert('playlists', playlist.toMap());
  }

  static Future<void> updatePlaylist(Playlist playlist) async {
    final db = await database;
    await db.update(
      'playlists',
      playlist.toMap(),
      where: 'id = ?',
      whereArgs: [playlist.id],
    );
  }

  static Future<void> deletePlaylist(int playlistId) async {
    final db = await database;
    await db.delete(
      'playlists',
      where: 'id = ?',
      whereArgs: [playlistId],
    );
  }

  static Future<List<Playlist>> getAllPlaylists() async {
    final db = await database;
    final maps = await db.query('playlists', orderBy: 'updatedAt DESC');
    return maps.map((m) => Playlist.fromMap(m)).toList();
  }

  static Future<Playlist?> getPlaylist(int id) async {
    final db = await database;
    final maps = await db.query(
      'playlists',
      where: 'id = ?',
      whereArgs: [id],
    );
    if (maps.isEmpty) return null;
    return Playlist.fromMap(maps.first);
  }

  static Future<void> addSongToPlaylist(PlaylistSong entry) async {
    final db = await database;
    await db.insert(
      'playlist_songs',
      entry.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
    await db.update(
      'playlists',
      {'updatedAt': DateTime.now().millisecondsSinceEpoch},
      where: 'id = ?',
      whereArgs: [entry.playlistId],
    );
  }

  static Future<void> removeSongFromPlaylist(int playlistId, int songId) async {
    final db = await database;
    await db.delete(
      'playlist_songs',
      where: 'playlistId = ? AND songId = ?',
      whereArgs: [playlistId, songId],
    );
    await db.update(
      'playlists',
      {'updatedAt': DateTime.now().millisecondsSinceEpoch},
      where: 'id = ?',
      whereArgs: [playlistId],
    );
  }

  static Future<List<int>> getSongIdsInPlaylist(int playlistId) async {
    final db = await database;
    final maps = await db.query(
      'playlist_songs',
      columns: ['songId'],
      where: 'playlistId = ?',
      whereArgs: [playlistId],
      orderBy: 'position ASC',
    );
    return maps.map((m) => m['songId'] as int).toList();
  }

  static Future<int> getPlaylistSongCount(int playlistId) async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT COUNT(*) as count FROM playlist_songs WHERE playlistId = ?',
      [playlistId],
    );
    return Sqflite.firstIntValue(result) ?? 0;
  }

  static Future<void> clearPlaylist(int playlistId) async {
    final db = await database;
    await db.delete(
      'playlist_songs',
      where: 'playlistId = ?',
      whereArgs: [playlistId],
    );
  }
}
