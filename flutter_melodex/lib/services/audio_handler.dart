import 'package:just_audio/just_audio.dart';
import 'package:audio_service/audio_service.dart';
import '../models/song.dart';

class MelodexAudioHandler extends BaseAudioHandler with SeekHandler {
  final AudioPlayer _player = AudioPlayer();
  final List<Song> _songs = [];
  int _currentIndex = -1;

  MelodexAudioHandler() {
    _init();
  }

  AudioPlayer get player => _player;
  List<Song> get songs => _songs;
  int get currentIndex => _currentIndex;

  Future<void> _init() async {
    _player.playerStateStream.listen((state) {
      _broadcastState();
    });

    _player.durationStream.listen((duration) {
      if (duration != null && mediaItem.value != null) {
        mediaItem.add(mediaItem.value!.copyWith(duration: duration));
      }
    });

    _player.positionStream.listen((_) {
      _broadcastState();
    });

    _player.currentIndexStream.listen((index) {
      if (index != null && index < _songs.length) {
        _currentIndex = index;
        mediaItem.add(_mediaItemFromSong(_songs[index]));
      }
    });

    _player.processingStateStream.listen((state) {
      if (state == ProcessingState.completed) {
        skipToNext();
      }
    });
  }

  MediaItem _mediaItemFromSong(Song song) {
    return MediaItem(
      id: song.uri,
      title: song.title,
      artist: song.artist,
      album: song.album,
      duration: Duration(milliseconds: song.duration),
      artUri: song.albumArtUri != null ? Uri.parse(song.albumArtUri!) : null,
    );
  }

  void _broadcastState() {
    playbackState.add(playbackState.value.copyWith(
      controls: [
        MediaControl.skipToPrevious,
        if (_player.playing) MediaControl.pause else MediaControl.play,
        MediaControl.skipToNext,
        MediaControl.stop,
      ],
      systemActions: const {
        MediaAction.seek,
        MediaAction.seekForward,
        MediaAction.seekBackward,
      },
      androidCompactActionIndices: const [0, 1, 2],
      processingState: _mapProcessingState(_player.processingState),
      playing: _player.playing,
      updatePosition: _player.position,
      bufferedPosition: _player.bufferedPosition,
      speed: _player.speed,
      queueIndex: _currentIndex,
    ));
  }

  AudioProcessingState _mapProcessingState(ProcessingState state) {
    switch (state) {
      case ProcessingState.idle:
        return AudioProcessingState.idle;
      case ProcessingState.loading:
        return AudioProcessingState.loading;
      case ProcessingState.buffering:
        return AudioProcessingState.buffering;
      case ProcessingState.ready:
        return AudioProcessingState.ready;
      case ProcessingState.completed:
        return AudioProcessingState.completed;
    }
  }

  Future<void> setQueue(List<Song> songs, {int startIndex = 0}) async {
    _songs.clear();
    _songs.addAll(songs);
    _currentIndex = startIndex;

    final audioSources = songs.map((song) {
      return AudioSource.uri(Uri.parse(song.uri));
    }).toList();

    final playlist = ConcatenatingAudioSource(children: audioSources);
    await _player.setAudioSource(playlist, initialIndex: startIndex);

    queue.add(songs.map(_mediaItemFromSong).toList());
    mediaItem.add(_mediaItemFromSong(songs[startIndex]));
  }

  Future<void> playSong(Song song) async {
    final index = _songs.indexWhere((s) => s.id == song.id);
    if (index >= 0) {
      _currentIndex = index;
      await _player.seek(Duration.zero, index: index);
      await _player.play();
    }
  }

  @override
  Future<void> play() async {
    await _player.play();
  }

  @override
  Future<void> pause() async {
    await _player.pause();
  }

  @override
  Future<void> stop() async {
    await _player.stop();
    await super.stop();
  }

  @override
  Future<void> seek(Duration position) async {
    await _player.seek(position);
  }

  @override
  Future<void> skipToNext() async {
    if (_currentIndex < _songs.length - 1) {
      _currentIndex++;
      await _player.seek(Duration.zero, index: _currentIndex);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_songs[_currentIndex]));
    }
  }

  @override
  Future<void> skipToPrevious() async {
    if (_currentIndex > 0) {
      _currentIndex--;
      await _player.seek(Duration.zero, index: _currentIndex);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_songs[_currentIndex]));
    }
  }

  Future<void> skipToIndex(int index) async {
    if (index >= 0 && index < _songs.length) {
      _currentIndex = index;
      await _player.seek(Duration.zero, index: index);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_songs[index]));
    }
  }

  Future<void> setShuffleMode(bool enabled) async {
    await _player.setShuffleModeEnabled(enabled);
  }

  Future<void> setRepeatMode(LoopMode mode) async {
    await _player.setLoopMode(mode);
  }

  Future<void> setSpeed(double speed) async {
    await _player.setSpeed(speed);
  }

  Duration get currentPosition => _player.position;
  Duration? get totalDuration => _player.duration;
  bool get isPlaying => _player.playing;

  @override
  Future<void> onTaskRemoved() async {
    await stop();
  }
}
