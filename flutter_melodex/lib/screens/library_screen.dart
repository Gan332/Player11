import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/library_provider.dart';
import '../providers/player_provider.dart';
import '../models/song.dart';

class LibraryScreen extends StatelessWidget {
  const LibraryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Library'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              showSearch(
                context: context,
                delegate: SongSearchDelegate(),
              );
            },
          ),
          PopupMenuButton<SortOption>(
            icon: const Icon(Icons.sort),
            onSelected: (option) {
              context.read<LibraryProvider>().setSortOption(option);
            },
            itemBuilder: (context) => const [
              PopupMenuItem(value: SortOption.titleAsc, child: Text('Title A-Z')),
              PopupMenuItem(value: SortOption.titleDesc, child: Text('Title Z-A')),
              PopupMenuItem(value: SortOption.artistAsc, child: Text('Artist A-Z')),
              PopupMenuItem(value: SortOption.shortest, child: Text('Shortest')),
              PopupMenuItem(value: SortOption.longest, child: Text('Longest')),
              PopupMenuItem(value: SortOption.newest, child: Text('Newest')),
              PopupMenuItem(value: SortOption.oldest, child: Text('Oldest')),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              // Import songs
            },
          ),
        ],
      ),
      body: Consumer<LibraryProvider>(
        builder: (context, library, _) {
          if (library.songs.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.music_note,
                    size: 64,
                    color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No songs found',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Import music files to get started',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: library.songs.length,
            padding: const EdgeInsets.only(bottom: 80),
            itemBuilder: (context, index) {
              final song = library.songs[index];
              return SongTile(
                song: song,
                isFavorite: library.isFavorite(song.id),
                onTap: () {
                  context.read<PlayerProvider>().setQueue(
                    library.songs,
                    startIndex: index,
                  );
                },
                onFavoriteToggle: () {
                  library.toggleFavorite(song.id);
                },
              );
            },
          );
        },
      ),
    );
  }
}

class SongTile extends StatelessWidget {
  final Song song;
  final bool isFavorite;
  final VoidCallback onTap;
  final VoidCallback onFavoriteToggle;

  const SongTile({
    super.key,
    required this.song,
    required this.isFavorite,
    required this.onTap,
    required this.onFavoriteToggle,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return ListTile(
      leading: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(8),
        ),
        child: song.albumArtUri != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.network(
                  song.albumArtUri!,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Icon(
                    Icons.music_note,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
              )
            : Icon(
                Icons.music_note,
                color: colorScheme.onPrimaryContainer,
              ),
      ),
      title: Text(
        song.title,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: Text(
        '${song.artist} • ${song.formattedDuration}',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(color: colorScheme.onSurfaceVariant),
      ),
      trailing: IconButton(
        icon: Icon(
          isFavorite ? Icons.favorite : Icons.favorite_border,
          color: isFavorite ? colorScheme.error : colorScheme.onSurfaceVariant,
        ),
        onPressed: onFavoriteToggle,
      ),
      onTap: onTap,
    );
  }
}

class SongSearchDelegate extends SearchDelegate {
  @override
  List<Widget> buildActions(BuildContext context) {
    return [
      IconButton(
        icon: const Icon(Icons.clear),
        onPressed: () {
          query = '';
        },
      ),
    ];
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.arrow_back),
      onPressed: () {
        close(context, null);
      },
    );
  }

  @override
  Widget buildResults(BuildContext context) {
    context.read<LibraryProvider>().search(query);
    return _buildSearchResults(context);
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    context.read<LibraryProvider>().search(query);
    return _buildSearchResults(context);
  }

  Widget _buildSearchResults(BuildContext context) {
    return Consumer<LibraryProvider>(
      builder: (context, library, _) {
        if (library.songs.isEmpty) {
          return const Center(
            child: Text('No results found'),
          );
        }

        return ListView.builder(
          itemCount: library.songs.length,
          itemBuilder: (context, index) {
            final song = library.songs[index];
            return SongTile(
              song: song,
              isFavorite: library.isFavorite(song.id),
              onTap: () {
                context.read<PlayerProvider>().setQueue(
                  library.songs,
                  startIndex: index,
                );
                close(context, null);
              },
              onFavoriteToggle: () {
                library.toggleFavorite(song.id);
              },
            );
          },
        );
      },
    );
  }
}
