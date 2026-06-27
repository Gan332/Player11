import sys, re
sys.stdout.reconfigure(encoding='utf-8')
with open(r'D:\MusicPlayer\flutter_melodex\lib\providers\player_provider.dart', 'r', encoding='utf-8') as f:
    c = f.read()

# Step 1: add dart:async import
c = c.replace("import 'package:just_audio/just_audio.dart';\nimport '../models/song.dart';", "import 'package:just_audio/just_audio.dart';\nimport 'dart:async';\nimport '../models/song.dart';")

# Step 2: add fields after equalizerPreset
c = c.replace('  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n\n  PlayerProvider', '  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n  // Sleep timer\n  bool _isSleepTimerActive = false;\n  int _sleepTimerRemainingMs = 0;\n  Timer? _sleepTimer;\n\n  // Queue management\n  List<Song> _originalQueue = [];\n  int _queueIndex = -1;\n\n  PlayerProvider')

# Step 3: add getters
c = c.replace('  int get equalizerPreset => _equalizerPreset;\n\n  void _init()', '  int get equalizerPreset => _equalizerPreset;\n  bool get isSleepTimerActive => _isSleepTimerActive;\n  int get sleepTimerRemainingMs => _sleepTimerRemainingMs;\n  List<Song> get queue => _audioHandler.queue;\n  int get queueIndex => _audioHandler.currentIndex;\n\n  void _init()')

# Step 4: setQueue
c = c.replace('    _currentSong = songs[startIndex];\n    _queueIndex = startIndex;\n    notifyListeners();\n  }\n\n  Future<void> playSong', '    _currentSong = songs[startIndex];\n    _originalQueue = List.from(songs);\n    _queueIndex = startIndex;\n    notifyListeners();\n  }\n\n  Future<void> playSong')

# Step 5: skip methods
c = c.replace('  Future<void> skipToNext() async {\n    await _audioHandler.skipToNext();\n  }\n\n  Future<void> skipToPrevious() async {\n    await _audioHandler.skipToPrevious();\n  }', '  Future<void> skipToNext() async {\n    await _audioHandler.skipToNext();\n    _queueIndex = _audioHandler.currentIndex;\n  }\n\n  Future<void> skipToPrevious() async {\n    await _audioHandler.skipToPrevious();\n    _queueIndex = _audioHandler.currentIndex;\n  }')

# Step 6: add queue mgmt + sleep timer
old_speed = '    notifyListeners();\n  }\n\n  Future<void> setPlaybackSpeed(double speed) async {'
new_qs = '    notifyListeners();\n  }\n\n  // Queue Management\n\n  List<Song> get upcomingSongs {\n    final q = _audioHandler.queue;\n    final idx = _audioHandler.currentIndex;\n    if (idx < 0 || idx >= q.length) return [];\n    return q.sublist(idx + 1);\n  }\n\n  List<Song> get previousSongs {\n    final q = _audioHandler.queue;\n    final idx = _audioHandler.currentIndex;\n    if (idx <= 0 || idx >= q.length) return [];\n    return q.sublist(0, idx);\n  }\n\n  Future<void> removeFromQueue(int index) async {\n    final q = List<Song>.from(_audioHandler.queue);\n    if (index < 0 || index >= q.length) return;\n    if (index == _audioHandler.currentIndex) return;\n    q.removeAt(index);\n    final currentIdx = _audioHandler.currentIndex;\n    final offset = index < currentIdx ? 1 : 0;\n    await _audioHandler.setQueue(q, startIndex: currentIdx - offset);\n    notifyListeners();\n  }\n\n  Future<void> clearQueue() async {\n    if (_audioHandler.queue.isEmpty) return;\n    final current = _audioHandler.queue[_audioHandler.currentIndex];\n    await _audioHandler.setQueue([current], startIndex: 0);\n    notifyListeners();\n  }\n\n  // Sleep Timer\n\n  void startSleepTimer(int durationMs) {\n    cancelSleepTimer();\n    _isSleepTimerActive = true;\n    _sleepTimerRemainingMs = durationMs;\n    notifyListeners();\n\n    _sleepTimer = Timer.periodic(const Duration(seconds: 1), (timer) {\n      _sleepTimerRemainingMs -= 1000;\n      if (_sleepTimerRemainingMs <= 0) {\n        _audioHandler.pause();\n        cancelSleepTimer();\n      }\n      notifyListeners();\n    });\n  }\n\n  void cancelSleepTimer() {\n    _sleepTimer?.cancel();\n    _sleepTimer = null;\n    _isSleepTimerActive = false;\n    _sleepTimerRemainingMs = 0;\n    notifyListeners();\n  }\n\n  String get formattedSleepTimer {\n    final totalSeconds = _sleepTimerRemainingMs ~/ 1000;\n    final hours = totalSeconds ~/ 3600;\n    final minutes = (totalSeconds % 3600) ~/ 60;\n    final seconds = totalSeconds % 60;\n    if (hours > 0) {\n      return "{hours}:{minutes.toString().padLeft(2, '0')}:{seconds.toString().padLeft(2, '0')}";\n    }\n    return "{minutes.toString().padLeft(2, '0')}:{seconds.toString().padLeft(2, '0')}";\n  }\n\n  Future<void> setPlaybackSpeed(double speed) async {'
c = c.replace(old_speed, new_qs)

# Step 7: equalizer
c = c.replace('  void setEqualizerPreset(int preset) {\n    _equalizerPreset = preset;\n    notifyListeners();\n  }', '  void setEqualizerPreset(int preset) {\n    _equalizerPreset = preset;\n    _equalizerEnabled = true;\n    notifyListeners();\n  }\n\n  void setEqualizerEnabled(bool enabled) {\n    _equalizerEnabled = enabled;\n    notifyListeners();\n  }')

with open(r'D:\MusicPlayer\flutter_melodex\lib\providers\player_provider.dart', 'w', encoding='utf-8') as f:
    f.write(c)
print('DONE')
