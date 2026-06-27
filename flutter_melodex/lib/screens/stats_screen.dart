import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/stats_provider.dart';

class StatsScreen extends StatelessWidget {
  const StatsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Statistics'),
      ),
      body: Consumer<StatsProvider>(
        builder: (context, stats, _) {
          return ListView(
            padding: const EdgeInsets.only(bottom: 80),
            children: [
              _buildSummaryCards(context, stats),
              const SizedBox(height: 24),
              _buildTopPlayedSection(context, stats),
              const SizedBox(height: 24),
              _buildRecentlyPlayedSection(context, stats),
            ],
          );
        },
      ),
    );
  }

  Widget _buildSummaryCards(BuildContext context, StatsProvider stats) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: _SummaryCard(
              title: 'Total Plays',
              value: stats.totalPlays.toString(),
              icon: Icons.play_circle_outline,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _SummaryCard(
              title: 'Unique Songs',
              value: stats.uniqueSongs.toString(),
              icon: Icons.music_note,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _SummaryCard(
              title: 'Listen Time',
              value: stats.formattedTotalListened,
              icon: Icons.timer_outlined,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTopPlayedSection(BuildContext context, StatsProvider stats) {
    if (stats.topPlayed.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            'Top Played',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        const SizedBox(height: 8),
        ...stats.topPlayed.asMap().entries.map((entry) {
          final index = entry.key;
          final stat = entry.value;
          return ListTile(
            leading: _RankBadge(rank: index + 1),
            title: Text(stat.title),
            subtitle: Text(stat.artist),
            trailing: Text(
              '${stat.playCount} plays',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          );
        }),
      ],
    );
  }

  Widget _buildRecentlyPlayedSection(BuildContext context, StatsProvider stats) {
    if (stats.recentlyPlayed.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            'Recently Played',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        const SizedBox(height: 8),
        ...stats.recentlyPlayed.map((stat) {
          final timeAgo = _formatTimeAgo(stat.lastPlayedAt);
          return ListTile(
            leading: Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(
                Icons.music_note,
                color: Theme.of(context).colorScheme.onPrimaryContainer,
              ),
            ),
            title: Text(stat.title),
            subtitle: Text(stat.artist),
            trailing: Text(
              timeAgo,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          );
        }),
      ],
    );
  }

  String _formatTimeAgo(int timestamp) {
    final now = DateTime.now().millisecondsSinceEpoch;
    final diff = now - timestamp;

    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return '${diff ~/ 60000}m ago';
    if (diff < 86400000) return '${diff ~/ 3600000}h ago';
    if (diff < 604800000) return '${diff ~/ 86400000}d ago';
    return '${(diff ~/ 604800000)}w ago';
  }
}

class _SummaryCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;

  const _SummaryCard({
    required this.title,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(icon, color: colorScheme.primary, size: 28),
            const SizedBox(height: 8),
            Text(
              value,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: colorScheme.primary,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              title,
              style: Theme.of(context).textTheme.bodySmall,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _RankBadge extends StatelessWidget {
  final int rank;

  const _RankBadge({required this.rank});

  @override
  Widget build(BuildContext context) {
    Color color;
    switch (rank) {
      case 1:
        color = const Color(0xFFFFD700); // Gold
        break;
      case 2:
        color = const Color(0xFFC0C0C0); // Silver
        break;
      case 3:
        color = const Color(0xFFCD7F32); // Bronze
        break;
      default:
        color = Theme.of(context).colorScheme.outline;
    }

    return Container(
      width: 32,
      height: 32,
      decoration: BoxDecoration(
        color: color.withOpacity(0.2),
        shape: BoxShape.circle,
        border: Border.all(color: color, width: 2),
      ),
      child: Center(
        child: Text(
          rank.toString(),
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ),
    );
  }
}
