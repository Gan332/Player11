class Playlist {
  final int? id;
  final String name;
  final int createdAt;
  final int updatedAt;

  const Playlist({
    this.id,
    required this.name,
    required this.createdAt,
    required this.updatedAt,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }

  factory Playlist.fromMap(Map<String, dynamic> map) {
    return Playlist(
      id: map['id'] as int?,
      name: map['name'] as String,
      createdAt: map['createdAt'] as int,
      updatedAt: map['updatedAt'] as int,
    );
  }

  Playlist copyWith({
    int? id,
    String? name,
    int? createdAt,
    int? updatedAt,
  }) {
    return Playlist(
      id: id ?? this.id,
      name: name ?? this.name,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}

class PlaylistSong {
  final int playlistId;
  final int songId;
  final int position;
  final int addedAt;

  const PlaylistSong({
    required this.playlistId,
    required this.songId,
    required this.position,
    required this.addedAt,
  });

  Map<String, dynamic> toMap() {
    return {
      'playlistId': playlistId,
      'songId': songId,
      'position': position,
      'addedAt': addedAt,
    };
  }

  factory PlaylistSong.fromMap(Map<String, dynamic> map) {
    return PlaylistSong(
      playlistId: map['playlistId'] as int,
      songId: map['songId'] as int,
      position: map['position'] as int,
      addedAt: map['addedAt'] as int,
    );
  }
}
