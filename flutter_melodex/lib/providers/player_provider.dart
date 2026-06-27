import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';
import '../models/song.dart';
import '../models/lyric_line.dart';
import '../services/audio_handler.dart';

class PlayerProvider extends ChangeNotifier {
  final MelodexAudioHandler _audioHandler;
  
  Song? _currentSong;
  bool _isPlaying = false;
  Duration _currentPosition = Duration.zero;
  Duration _duration = Duration.zero;
  bool _isShuffled = false;
  LoopMode _repeatMode = LoopMode.off;
  List<LyricLine> _lyrics = [];
  double _playbackSpeed = 1.0;
  bool _equalizerEnabled = false;
  int _equalizerPreset = 0;

  PlayerProvider(this._audioHandler) {
    _init();
  }

  Song? get currentSong => _currentSong;
  bool get isPlaying => _isPlaying;
  Duration get currentPosition => _currentPosition;
  Duration get duration => _duration;
  bool get isShuffled => _isShuffled;
  LoopMode get repeatMode => _repeatMode;
  List<LyricLine> get lyrics => _lyrics;
  double get playbackSpeed => _playbackSpeed;
  bool get equalizerEnabled => _equalizerEnabled;
  int get equalizerPreset => _equalizerPreset;

  void _init() {
    _audioHandler.player.playingStream.listen((playing) {
      _isPlaying = playing;
      notifyListeners();
    });

    _audioHandler.player.positionStream.listen((position) {
      _currentPosition = position;
      notifyListeners();
    });

    _audioHandler.player.durationStream.listen((duration) {
      if (duration != null) {
        _duration = duration;
        notifyListeners();
      }
    });

    _audioHandler.player.currentIndexStream.listen((index) {
      if (index != null && index < _audioHandler.queue.length) {
        _currentSong = _audioHandler.queue[index];
        notifyListeners();
      }
    });
  }

  Future<void> setQueue(List<Song> songs, {int startIndex = 0}) async {
    await _audioHandler.setQueue(songs, startIndex: startIndex);
    _currentSong = songs[startIndex];
    notifyListeners();
  }

  Future<void> playSong(Song song) async {
    await _audioHandler.playSong(song);
    _currentSong = song;
    notifyListeners();
  }

  Future<void> play() async {
    await _audioHandler.play();
  }

  Future<void> pause() async {
    await _audioHandler.pause();
  }

  Future<void> togglePlayPause() async {
    if (_isPlaying) {
      await pause();
    } else {
      await play();
    }
  }

  Future<void> seekTo(Duration position) async {
    await _audioHandler.seek(position);
  }

  Future<void> skipToNext() async {
    await _audioHandler.skipToNext();
  }

  Future<void> skipToPrevious() async {
    await _audioHandler.skipToPrevious();
  }

  Future<void> toggleShuffle() async {
    _isShuffled = !_isShuffled;
    await _audioHandler.setShuffleMode(_isShuffled);
    notifyListeners();
  }

  Future<void> cycleRepeatMode() async {
    switch (_repeatMode) {
      case LoopMode.off:
        _repeatMode = LoopMode.one;
        break;
      case LoopMode.one:
        _repeatMode = LoopMode.all;
        break;
      case LoopMode.all:
        _repeatMode = LoopMode.off;
        break;
    }
    await _audioHandler.setRepeatMode(_repeatMode);
    notifyListeners();
  }

  Future<void> setPlaybackSpeed(double speed) async {
    _playbackSpeed = speed.clamp(0.5, 2.0);
    await _audioHandler.setSpeed(_playbackSpeed);
    notifyListeners();
  }

  void setLyrics(List<LyricLine> lyrics) {
    _lyrics = lyrics;
    notifyListeners();
  }

  void toggleEqualizer() {
    _equalizerEnabled = !_equalizerEnabled;
    notifyListeners();
  }

  void setEqualizerPreset(int preset) {
    _equalizerPreset = preset;
    notifyListeners();
  }

  String get formattedPosition {
    final minutes = _currentPosition.inMinutes;
    final seconds = _currentPosition.inSeconds % 60;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  String get formattedDuration {
    final minutes = _duration.inMinutes;
    final seconds = _duration.inSeconds % 60;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }
}
