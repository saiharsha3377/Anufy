package com.aurora.music.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import kotlin.math.max

data class PlayerSnapshot(
    val title: String,
    val artist: String,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val shuffle: Boolean,
    val repeatMode: Int,
    val hasQueue: Boolean,
) {
    companion object {
        fun idle(): PlayerSnapshot = PlayerSnapshot(
            title = "",
            artist = "",
            isPlaying = false,
            positionMs = 0L,
            durationMs = 0L,
            shuffle = false,
            repeatMode = Player.REPEAT_MODE_OFF,
            hasQueue = false,
        )

        fun from(player: Player): PlayerSnapshot {
            val meta = player.mediaMetadata
            val title = meta.title?.toString().orEmpty().ifBlank { "Unknown title" }
            val artist = meta.artist?.toString().orEmpty().ifBlank { "Unknown artist" }
            val duration = if (player.duration > 0) player.duration else 0L
            val position = player.currentPosition.coerceAtLeast(0L)
            return PlayerSnapshot(
                title = title,
                artist = artist,
                isPlaying = player.isPlaying,
                positionMs = position,
                durationMs = duration,
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode,
                hasQueue = player.mediaItemCount > 0,
            )
        }
    }
}

@Composable
fun rememberPlayerSnapshot(player: Player?): PlayerSnapshot {
    var snapshot by remember { mutableStateOf(PlayerSnapshot.idle()) }

    DisposableEffect(player) {
        if (player == null) {
            snapshot = PlayerSnapshot.idle()
            return@DisposableEffect onDispose { }
        }

        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                snapshot = PlayerSnapshot.from(player)
            }
        }

        player.addListener(listener)
        snapshot = PlayerSnapshot.from(player)

        onDispose {
            player.removeListener(listener)
        }
    }

    return snapshot
}

fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = max(0L, ms / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
