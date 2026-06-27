import re

with open(r'D:\MusicPlayer\flutter_melodex\lib\providers\player_provider.dart', 'r', encoding='utf-8') as f:
    content = f.read()

# Step 1: add dart:async import
content = content.replace(
    "import 'package:just_audio/just_audio.dart';\nimport '../models/song.dart';",
    "import 'package:just_audio/just_audio.dart';\nimport 'dart:async';\nimport '../models/song.dart';"
)

# Step 2: add fields after equalizerPreset
content = content.replace(
    '  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n\n  PlayerProvider',
    '  bool _equalizerEnabled = false;\n  int _equalizerPreset = 0;\n  // Sleep timer\n  bool _isSleepTimerActive = false;\n  int _sleepTimerRemainingMs = 0;\n  Timer? _sleepTimer;\n\n  // Queue management\n  List<Song> _originalQueue = [];\n  int _queueIndex = -1;\n\n  PlayerProvider'
)

print("Step 1-2 done")
