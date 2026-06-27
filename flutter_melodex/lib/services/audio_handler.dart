import 'package:just_audio/just_audio.dart';
import 'package:audio_service/audio_service.dart';
import '../models/song.dart';

class MelodexAudioHandler extends BaseAudioHandler with SeekHandler {
  final AudioPlayer _player = AudioPlayer();
  List<Song> _queue = [];
  int _currentIndex = -1;

  MelodexAudioHandler() {
    _init();
  }

  AudioPlayer get player => _player;
  List<Song> get queue => _queue;
  int get currentIndex => _currentIndex;

  Future<void> _init() async {
    _player.playbackEventStream.listen((event) {
      _broadcastState();
    });

    _player.durationStream.listen((duration) {
      if (duration != null) {
        mediaItem.add(mediaItem.value!.copyWith(duration: duration));
      }
    });

    _player.positionStream.listen((position) {
      _broadcastState();
    });

    _player.currentIndexStream.listen((index) {
      if (index != null && index < _queue.length) {
        _currentIndex = index;
        mediaItem.add(_mediaItemFromSong(_queue[index]));
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
      processingState: const {
        ProcessingState.idle: AudioProcessingState.idle,
        ProcessingState.loading: AudioProcessingState.loading,
        ProcessingState.buffering: AudioProcessingState.buffering,
        ProcessingState.ready: AudioProcessingState.ready,
        ProcessingState.completed: AudioProcessingState.completed,
      }[_player.processingState]!,
      playing: _player.playing,
      updatePosition: _player.position,
      bufferedPosition: _player.bufferedPosition,
      speed: _player.speed,
      queueIndex: _currentIndex,
    ));
  }

  Future<void> setQueue(List<Song> songs, {int startIndex = 0}) async {
    _queue = songs;
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
    final index = _queue.indexWhere((s) => s.id == song.id);
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
    if (_currentIndex < _queue.length - 1) {
      _currentIndex++;
      await _player.seek(Duration.zero, index: _currentIndex);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_queue[_currentIndex]));
    }
  }

  @override
  Future<void> skipToPrevious() async {
    if (_currentIndex > 0) {
      _currentIndex--;
      await _player.seek(Duration.zero, index: _currentIndex);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_queue[_currentIndex]));
    }
  }

  Future<void> skipToIndex(int index) async {
    if (index >= 0 && index < _queue.length) {
      _currentIndex = index;
      await _player.seek(Duration.zero, index: index);
      await _player.play();
      mediaItem.add(_mediaItemFromSong(_queue[index]));
    }
  }

  Future<void> setShuffleMode(bool enabled) async {
    if (enabled) {
      await _player.setShuffleModeEnabled(true);
      await _player.shuffle();
    } else {
      await _player.setShuffleModeEnabled(false);
    }
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
