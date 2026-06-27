import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:just_audio/just_audio.dart';
import '../providers/player_provider.dart';

class PlayerControls extends StatelessWidget {
  const PlayerControls({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Consumer<PlayerProvider>(
      builder: (context, player, _) {
        return Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            // Shuffle
            IconButton(
              icon: Icon(
                Icons.shuffle,
                color: player.isShuffled
                    ? colorScheme.primary
                    : colorScheme.onSurfaceVariant,
              ),
              onPressed: () {
                player.toggleShuffle();
              },
            ),
            // Previous
            IconButton(
              icon: const Icon(Icons.skip_previous),
              iconSize: 36,
              onPressed: () {
                player.skipToPrevious();
              },
            ),
            // Play/Pause
            Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: colorScheme.primary,
                boxShadow: [
                  BoxShadow(
                    color: colorScheme.primary.withOpacity(0.3),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: IconButton(
                icon: Icon(
                  player.isPlaying ? Icons.pause : Icons.play_arrow,
                  color: colorScheme.onPrimary,
                ),
                iconSize: 48,
                onPressed: () {
                  player.togglePlayPause();
                },
              ),
            ),
            // Next
            IconButton(
              icon: const Icon(Icons.skip_next),
              iconSize: 36,
              onPressed: () {
                player.skipToNext();
              },
            ),
            // Repeat
            IconButton(
              icon: Icon(
                _getRepeatIcon(player.repeatMode),
                color: player.repeatMode != LoopMode.off
                    ? colorScheme.primary
                    : colorScheme.onSurfaceVariant,
              ),
              onPressed: () {
                player.cycleRepeatMode();
              },
            ),
          ],
        );
      },
    );
  }

  IconData _getRepeatIcon(LoopMode mode) {
    switch (mode) {
      case LoopMode.off:
        return Icons.repeat;
      case LoopMode.one:
        return Icons.repeat_one;
      case LoopMode.all:
        return Icons.repeat;
    }
  }
}
