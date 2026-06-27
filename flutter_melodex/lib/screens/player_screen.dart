import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:just_audio/just_audio.dart';
import '../providers/player_provider.dart';
import '../providers/library_provider.dart';
import '../widgets/player_controls.dart';

class PlayerScreen extends StatelessWidget {
  const PlayerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Now Playing'),
        actions: [
          IconButton(
            icon: const Icon(Icons.timer_outlined),
            onPressed: () {
              // Show sleep timer dialog
            },
          ),
        ],
      ),
      body: Consumer<PlayerProvider>(
        builder: (context, player, _) {
          if (player.currentSong == null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.play_circle_outline,
                    size: 64,
                    color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No song playing',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Select a song from your library',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            );
          }

          final song = player.currentSong!;
          final colorScheme = Theme.of(context).colorScheme;

          return Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                const Spacer(),
                // Album Art
                Container(
                  width: 280,
                  height: 280,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(20),
                    color: colorScheme.primaryContainer,
                    boxShadow: [
                      BoxShadow(
                        color: colorScheme.shadow.withOpacity(0.2),
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: song.albumArtUri != null
                      ? ClipRRect(
                          borderRadius: BorderRadius.circular(20),
                          child: Image.network(
                            song.albumArtUri!,
                            fit: BoxFit.cover,
                            errorBuilder: (_, __, ___) => Icon(
                              Icons.music_note,
                              size: 80,
                              color: colorScheme.onPrimaryContainer,
                            ),
                          ),
                        )
                      : Icon(
                          Icons.music_note,
                          size: 80,
                          color: colorScheme.onPrimaryContainer,
                        ),
                ),
                const SizedBox(height: 32),
                // Song Info
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            song.title,
                            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 4),
                          Text(
                            song.artist,
                            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                              color: colorScheme.onSurfaceVariant,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                    Consumer<LibraryProvider>(
                      builder: (context, library, _) {
                        final isFav = library.isFavorite(song.id);
                        return IconButton(
                          icon: Icon(
                            isFav ? Icons.favorite : Icons.favorite_border,
                            color: isFav ? colorScheme.error : colorScheme.onSurfaceVariant,
                          ),
                          onPressed: () {
                            library.toggleFavorite(song.id);
                          },
                        );
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                // Progress Slider
                Column(
                  children: [
                    Slider(
                      value: player.currentPosition.inMilliseconds.toDouble(),
                      max: player.duration.inMilliseconds.toDouble().clamp(1, double.infinity),
                      onChanged: (value) {
                        player.seekTo(Duration(milliseconds: value.toInt()));
                      },
                    ),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            player.formattedPosition,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                          Text(
                            player.formattedDuration,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                // Transport Controls
                const PlayerControls(),
                const Spacer(),
              ],
            ),
          );
        },
      ),
    );
  }
}
