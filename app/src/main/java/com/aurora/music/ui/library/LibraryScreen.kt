package com.aurora.music.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.data.Track
import com.aurora.music.ui.player.LocalAuroraPlayer
import com.aurora.music.ui.player.formatTime

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    audioPermissionGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    val player = LocalAuroraPlayer.current
    val tracks = viewModel.filteredTracks()

    if (!audioPermissionGranted) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Allow music access",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Aurora reads audio files already on your phone (similar to a downloads player). Grant storage/audio permission to see your library.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Your library",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        Text(
            text = "On-device music, clean playback, no interruptions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        OutlinedTextField(
            value = viewModel.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            placeholder = { Text("Search songs, artists, albums") },
        )

        if (tracks.isEmpty()) {
            Text(
                text = "No on-device music found yet. Copy MP3/FLAC files to your phone, then pull to refresh (restart the app after adding files).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                TrackRow(
                    track = track,
                    onClick = {
                        if (player.ready) {
                            player.playQueue(tracks, index)
                        }
                    },
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatTime(track.durationMs),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
