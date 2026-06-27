class PlayStat {
  final int songId;
  final String title;
  final String artist;
  final String? albumArtUri;
  final bool isFavorite;
  final int playCount;
  final int skipCount;
  final int lastPlayedAt;
  final int totalPlayedMs;

  const PlayStat({
    required this.songId,
    required this.title,
    required this.artist,
    this.albumArtUri,
    this.isFavorite = false,
    this.playCount = 0,
    this.skipCount = 0,
    this.lastPlayedAt = 0,
    this.totalPlayedMs = 0,
  });

  Map<String, dynamic> toMap() {
    return {
      'songId': songId,
      'title': title,
      'artist': artist,
      'albumArtUri': albumArtUri,
      'isFavorite': isFavorite ? 1 : 0,
      'playCount': playCount,
      'skipCount': skipCount,
      'lastPlayedAt': lastPlayedAt,
      'totalPlayedMs': totalPlayedMs,
    };
  }

  factory PlayStat.fromMap(Map<String, dynamic> map) {
    return PlayStat(
      songId: map['songId'] as int,
      title: map['title'] as String,
      artist: map['artist'] as String,
      albumArtUri: map['albumArtUri'] as String?,
      isFavorite: (map['isFavorite'] as int) == 1,
      playCount: map['playCount'] as int,
      skipCount: map['skipCount'] as int,
      lastPlayedAt: map['lastPlayedAt'] as int,
      totalPlayedMs: map['totalPlayedMs'] as int,
    );
  }

  PlayStat copyWith({
    int? songId,
    String? title,
    String? artist,
    String? albumArtUri,
    bool? isFavorite,
    int? playCount,
    int? skipCount,
    int? lastPlayedAt,
    int? totalPlayedMs,
  }) {
    return PlayStat(
      songId: songId ?? this.songId,
      title: title ?? this.title,
      artist: artist ?? this.artist,
      albumArtUri: albumArtUri ?? this.albumArtUri,
      isFavorite: isFavorite ?? this.isFavorite,
      playCount: playCount ?? this.playCount,
      skipCount: skipCount ?? this.skipCount,
      lastPlayedAt: lastPlayedAt ?? this.lastPlayedAt,
      totalPlayedMs: totalPlayedMs ?? this.totalPlayedMs,
    );
  }
}
