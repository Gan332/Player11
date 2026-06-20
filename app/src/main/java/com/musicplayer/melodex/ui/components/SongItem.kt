package com.musicplayer.melodex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musicplayer.melodex.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 56.dp
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "${song.artist} · ${song.formattedDuration}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AlbumArt(
                albumArtUri = song.albumArtUri,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(RoundedCornerShape(12.dp))
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
fun AlbumArt(
    albumArtUri: android.net.Uri?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (albumArtUri != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
