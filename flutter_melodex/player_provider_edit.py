with open(r'D:\MusicPlayer\flutter_melodex\lib\providers\player_provider.dart', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    # Step 1: add dart:async import
    if line == "import 'package:just_audio/just_audio.dart';\n":
        new_lines.append(line)
        new_lines.append("import 'dart:async';\n")
        continue
    new_lines.append(line)

content = ''.join(new_lines)

# Step 2: add fields after _equalizerPreset line
content = content.replace(
    "  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n\n  PlayerProvider",
    "  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n  // Sleep timer\n  bool _isSleepTimerActive = false;\n  int _sleepTimerRemainingMs = 0;\n  Timer? _sleepTimer;\n\n  // Queue management\n  List<Song> _originalQueue = [];\n  int _queueIndex = -1;\n\n  PlayerProvider"
)

# Step 3: add getters
content = content.replace(
    "  int get equalizerPreset => _equalizerPreset;\n\n  void _init()",
    "  int get equalizerPreset => _equalizerPreset;\n  bool get isSleepTimerActive => _isSleepTimerActive;\n  int get sleepTimerRemainingMs => _sleepTimerRemainingMs;\n  List<Song> get queue => _audioHandler.queue;\n  int get queueIndex => _audioHandler.currentIndex;\n\n  void _init()"
)

# Step 4: setQueue
content = content.replace(
    '    _currentSong = songs[startIndex];\n    notifyListeners();\n  }\n\n  Future<void> playSong',
    '    _currentSong = songs[startIndex];\n    _originalQueue = List.from(songs);\n    _queueIndex = startIndex;\n    notifyListeners();\n  }\n\n  Future<void> playSong'
)

# Step 5: skip methods
content = content.replace(
    '  Future<void> skipToNext() async {\n    await _audioHandler.skipToNext();\n  }\n\n  Future<void> skipToPrevious() async {\n    await _audioHandler.skipToPrevious();\n  }',
    '  Future<void> skipToNext() async {\n    await _audioHandler.skipToNext();\n    _queueIndex = _audioHandler.currentIndex;\n  }\n\n  Future<void> skipToPrevious() async {\n    await _audioHandler.skipToPrevious();\n    _queueIndex = _audioHandler.currentIndex;\n  }'
)

# Step 6: equalizer
content = content.replace(
    '  void setEqualizerPreset(int preset) {\n    _equalizerPreset = preset;\n    notifyListeners();\n  }',
    '  void setEqualizerPreset(int preset) {\n    _equalizerPreset = preset;\n    _equalizerEnabled = true;\n    notifyListeners();\n  }\n\n  void setEqualizerEnabled(bool enabled) {\n    _equalizerEnabled = enabled;\n    notifyListeners();\n  }'
)

# Step 7: Add queue mgmt + sleep timer before setPlaybackSpeed
old_speed = '    notifyListeners();\n  }\n\n  Future<void> setPlaybackSpeed(double speed) async {'

qs_block = '''
  }

  // ── Queue Management ──

  List<Song> get upcomingSongs {
    final q = _audioHandler.queue;
    final idx = _audioHandler.currentIndex;
    if (idx < 0 || idx >= q.length) return [];
    return q.sublist(idx + 1);
  }

  List<Song> get previousSongs {
    final q = _audioHandler.queue;
    final idx = _audioHandler.currentIndex;
    if (idx <= 0 || idx >= q.length) return [];
    return q.sublist(0, idx);
  }

  Future<void> removeFromQueue(int index) async {
    final q = List<Song>.from(_audioHandler.queue);
    if (index < 0 || index >= q.length) return;
    if (index == _audioHandler.currentIndex) return;
    q.removeAt(index);
    final currentIdx = _audioHandler.currentIndex;
    final offset = index < currentIdx ? 1 : 0;
    await _audioHandler.setQueue(q, startIndex: currentIdx - offset);
    notifyListeners();
  }

  Future<void> clearQueue() async {
    if (_audioHandler.queue.isEmpty) return;
    final current = _audioHandler.queue[_audioHandler.currentIndex];
    await _audioHandler.setQueue([current], startIndex: 0);
    notifyListeners();
  }

  // ── Sleep Timer ──

  void startSleepTimer(int durationMs) {
    cancelSleepTimer();
    _isSleepTimerActive = true;
    _sleepTimerRemainingMs = durationMs;
    notifyListeners();

    _sleepTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _sleepTimerRemainingMs -= 1000;
      if (_sleepTimerRemainingMs <= 0) {
        _audioHandler.pause();
        cancelSleepTimer();
      }
      notifyListeners();
    });
  }

  void cancelSleepTimer() {
    _sleepTimer?.cancel();
    _sleepTimer = null;
    _isSleepTimerActive = false;
    _sleepTimerRemainingMs = 0;
    notifyListeners();
  }

  String get formattedSleepTimer {
    final totalSeconds = _sleepTimerRemainingMs ~/ 1000;
    final hours = totalSeconds ~/ 3600;
    final minutes = (totalSeconds % 3600) ~/ 60;
    final seconds = totalSeconds % 60;
    if (hours > 0) {
      return '$hours:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    }
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  Future<void> setPlaybackSpeed(double speed) async {
'''
content = content.replace(old_speed, qs_block)

with open(r'D:\MusicPlayer\flutter_melodex\lib\providers\player_provider.dart', 'w', encoding='utf-8') as f:
    f.write(content)

print('PlayerProvider: ALL STEPS DONE')
