# Melodex

A modern Android music player built with **Material Design 3 (MD3)** and **Jetpack Compose**, featuring dynamic theming and GitHub Actions CI builds.

## Features

- **Material Design 3** — Uses MD3 design tokens, dynamic color, and Material You
- **Local music playback** — Plays audio files stored on your device
- **Adjustable theme** — Light/Dark/System mode, dynamic colors, custom seed color picker
- **Library search** — Search through your songs by title, artist, or album
- **Playback controls** — Play, pause, skip, seek, shuffle, and repeat
- **Album art** — Displays embedded album artwork from your music files

## Screens

| Screen | Description |
|--------|-------------|
| **Library** | Browse and search your local music library |
| **Player** | Now Playing view with album art and transport controls |
| **Settings** | Theme customization and app info |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Audio**: Media3 (ExoPlayer)
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Preferences**: DataStore
- **Build**: Gradle with version catalog
- **CI**: GitHub Actions

## Prerequisites

- Android Studio Hedgehog or later
- JDK 17+
- Android SDK 35
- Gradle 8.11.1

## Build

### Local

```bash
# Generate Gradle wrapper (first time)
gradle wrapper --gradle-version 8.11.1

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### GitHub Actions

The included `.github/workflows/build.yml` workflow will:
1. Set up JDK 17 and Gradle
2. Build the debug APK
3. Upload the APK as a build artifact
4. Run lint checks

Push to `main` or open a PR to trigger automated builds.

## Project Structure

```
app/
  src/main/
    java/com/musicplayer/melodex/
      MainActivity.kt          # Entry point, navigation
      MelodexApp.kt             # Application class
      data/
        model/Song.kt           # Song data model
        repository/MusicRepository.kt  # MediaStore scanner
        preference/ThemePreferences.kt  # Theme settings persistence
      player/
        MusicPlayerController.kt  # ExoPlayer wrapper
      ui/
        theme/Color.kt, Type.kt, Theme.kt  # MD3 theming
        navigation/NavGraph.kt    # Route definitions
        screens/                  # Library, Player, Settings
        components/               # SongItem, PlayerControls

.github/workflows/build.yml     # CI build pipeline
```

## Theme Customization

Go to **Settings** to customize:
- **Theme mode**: System default, Light, or Dark
- **Dynamic colors**: On Android 12+, uses your wallpaper palette
- **Seed color**: Pick a custom color to generate the palette

## License

MIT
