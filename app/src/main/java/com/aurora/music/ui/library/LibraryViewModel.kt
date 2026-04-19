package com.aurora.music.ui.library

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.aurora.music.data.LocalLibraryRepository
import com.aurora.music.data.Track

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalLibraryRepository(application)

    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set

    var query by mutableStateOf("")
        private set

    fun updateQuery(value: String) {
        query = value
    }

    fun refreshLibrary() {
        tracks = repository.loadTracks()
    }

    fun filteredTracks(): List<Track> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return tracks
        return tracks.filter {
            it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
        }
    }
}
