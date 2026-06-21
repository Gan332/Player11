package com.musicplayer.melodex.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.musicplayer.melodex.data.model.Song
import com.musicplayer.melodex.ui.components.SongItem

/** 搜索筛选字段 */
private enum class SearchFilter(val label: String) {
    ALL("All"),
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album")
}

/** 排序方式 */
private enum class SortOption(val label: String) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    ARTIST_ASC("Artist A-Z"),
    DURATION_ASC("Shortest"),
    DURATION_DESC("Longest"),
    DATE_ASC("Newest"),
    DATE_DESC("Oldest")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlayerNavigate: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var activeSort by remember { mutableStateOf(SortOption.TITLE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredAndSortedSongs = remember(songs, searchQuery, activeFilter, activeSort) {
        val filtered = if (searchQuery.isBlank()) songs
        else songs.filter { song ->
            when (activeFilter) {
                SearchFilter.ALL -> song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true) ||
                    song.album.contains(searchQuery, ignoreCase = true)
                SearchFilter.TITLE -> song.title.contains(searchQuery, ignoreCase = true)
                SearchFilter.ARTIST -> song.artist.contains(searchQuery, ignoreCase = true)
                SearchFilter.ALBUM -> song.album.contains(searchQuery, ignoreCase = true)
            }
        }
        when (activeSort) {
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase() }
            SortOption.DURATION_ASC -> filtered.sortedBy { it.duration }
            SortOption.DURATION_DESC -> filtered.sortedByDescending { it.duration }
            SortOption.DATE_ASC -> filtered.sortedByDescending { it.dateAdded }
            SortOption.DATE_DESC -> filtered.sortedBy { it.dateAdded }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onImportClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import songs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentSong != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                SmallFloatingActionButton(
                    onClick = onPlayerNavigate,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Now Playing",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Search Bar ──
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search ${activeFilter.label.lowercase()}...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                )
            )

            // ── Filter Chips + Sort ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    SearchFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { activeFilter = filter },
                            label = {
                                Text(
                                    filter.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = if (activeFilter == filter) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = MaterialTheme.shapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.Outlined.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        Text(
                            "Sort by",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (activeSort == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    activeSort = option
                                    showSortMenu = false
                                },
                                leadingIcon = if (activeSort == option) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                } else null
                            )
                        }
                    }
                }
            }

            // ── Song count ──
            Text(
                text = "${filteredAndSortedSongs.size} songs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // ── Song List ──
            if (filteredAndSortedSongs.isEmpty()) {
                EmptyLibrary(
                    isSearching = searchQuery.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredAndSortedSongs,
                        key = { it.id },
                        contentType = { "song" }
                    ) { song ->
                        SongItem(
                            song = song,
                            isPlaying = currentSong?.id == song.id && isPlaying,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isSearching) "No results found" else "Your library is empty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearching) "Try a different search term or filter"
                else "Tap + to import music files",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}