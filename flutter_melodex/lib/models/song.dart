class Song {
  final int id;
  final String title;
  final String artist;
  final String album;
  final int duration;
  final String uri;
  final String? albumArtUri;
  final int size;
  final int dateAdded;

  const Song({
    required this.id,
    required this.title,
    required this.artist,
    required this.album,
    required this.duration,
    required this.uri,
    this.albumArtUri,
    required this.size,
    required this.dateAdded,
  });

  String get formattedDuration {
    final minutes = duration ~/ 60000;
    final seconds = (duration % 60000) ~/ 1000;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'artist': artist,
      'album': album,
      'duration': duration,
      'uri': uri,
      'albumArtUri': albumArtUri,
      'size': size,
      'dateAdded': dateAdded,
    };
  }

  factory Song.fromMap(Map<String, dynamic> map) {
    return Song(
      id: map['id'] as int,
      title: map['title'] as String,
      artist: map['artist'] as String,
      album: map['album'] as String,
      duration: map['duration'] as int,
      uri: map['uri'] as String,
      albumArtUri: map['albumArtUri'] as String?,
      size: map['size'] as int,
      dateAdded: map['dateAdded'] as int,
    );
  }

  Song copyWith({
    int? id,
    String? title,
    String? artist,
    String? album,
    int? duration,
    String? uri,
    String? albumArtUri,
    int? size,
    int? dateAdded,
  }) {
    return Song(
      id: id ?? this.id,
      title: title ?? this.title,
      artist: artist ?? this.artist,
      album: album ?? this.album,
      duration: duration ?? this.duration,
      uri: uri ?? this.uri,
      albumArtUri: albumArtUri ?? this.albumArtUri,
      size: size ?? this.size,
      dateAdded: dateAdded ?? this.dateAdded,
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Song && runtimeType == other.runtimeType && id == other.id;

  @override
  int get hashCode => id.hashCode;
}
