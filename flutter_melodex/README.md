# Melodex Flutter

A Material Design 3 music player built with Flutter.

## Features

- Music playback with just_audio
- Background playback with audio_service
- MediaStore integration for local music
- File import support
- Playlists management
- Favorites
- Play statistics
- Sleep timer
- Lyrics display (LRC format)
- Material Design 3 theming
- Dark/Light mode support
- Custom seed color

## Getting Started

### Prerequisites

- Flutter 3.24.0 or later
- Dart 3.2.0 or later

### Installation

```bash
cd flutter_melodex
flutter pub get
flutter run
```

### Building

```bash
# Android APK
flutter build apk --release

# iOS
flutter build ios --release
```

## Project Structure

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
│   ├── song.dart
│   ├── lyric_line.dart
│   ├── play_stat.dart
│   └── playlist.dart
├── providers/                # State management
│   ├── player_provider.dart
│   ├── library_provider.dart
│   ├── playlist_provider.dart
│   ├── stats_provider.dart
│   └── theme_provider.dart
├── services/                 # Business logic
│   ├── audio_handler.dart
│   └── database_service.dart
├── screens/                  # UI screens
│   ├── main_screen.dart
│   ├── library_screen.dart
│   ├── player_screen.dart
│   ├── playlist_screen.dart
│   ├── settings_screen.dart
│   └── stats_screen.dart
├── widgets/                  # Reusable widgets
│   ├── player_controls.dart
│   └── mini_player.dart
├── theme/                    # Theming
│   └── app_theme.dart
└── utils/                    # Utilities
    └── lyrics_parser.dart
```

## Architecture

- **State Management**: Provider
- **Audio**: just_audio + audio_service
- **Database**: sqflite
- **Navigation**: Bottom navigation with IndexedStack

## Migration from Android

This Flutter app is a complete rewrite of the original Android/Kotlin Melodex app. Key differences:

| Android | Flutter |
|---------|---------|
| ExoPlayer | just_audio |
| MediaSession | audio_service |
| Room Database | sqflite |
| DataStore | shared_preferences |
| Jetpack Compose | Flutter Widgets |
| StateFlow | ChangeNotifier + Provider |

## CI/CD

GitHub Actions automatically builds:
- Android APK on push to main/master
- iOS build (no codesign) on push to main/master

See `.github/workflows/flutter-build.yml` for details.
