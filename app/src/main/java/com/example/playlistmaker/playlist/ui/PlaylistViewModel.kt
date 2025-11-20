package com.example.playlistmaker.playlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistInteractor
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.launch

data class PlaylistState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: Long = 0L,
    val isLoading: Boolean = false
)

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistState>()
    val state: LiveData<PlaylistState> = _state

    private val _isPlaylistDeleted = MutableLiveData<Boolean>()
    val isPlaylistDeleted: LiveData<Boolean> = _isPlaylistDeleted

    private var currentPlaylistId: Long = 0

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true) ?: PlaylistState(isLoading = true)
            
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            val tracks = playlistInteractor.getPlaylistTracks(playlistId)
            val totalDuration = tracks.sumOf { it.trackTimeMillis }
            
            _state.value = PlaylistState(
                playlist = playlist,
                tracks = tracks,
                totalDuration = totalDuration,
                isLoading = false
            )
        }
    }

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(currentPlaylistId, trackId)
            // Перезагружаем данные плейлиста
            loadPlaylist(currentPlaylistId)
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            try {
                playlistInteractor.deletePlaylist(currentPlaylistId)
                _isPlaylistDeleted.postValue(true)
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error deleting playlist", e)
                _isPlaylistDeleted.postValue(false)
            }
        }
    }

    fun getPlaylistShareText(resources: android.content.res.Resources): String? {
        val state = _state.value ?: return null
        val playlist = state.playlist ?: return null
        val tracks = state.tracks
        
        if (tracks.isEmpty()) return null
        
        val builder = StringBuilder()
        builder.append(playlist.name)
        
        if (!playlist.description.isNullOrEmpty()) {
            builder.append("\n").append(playlist.description)
        }
        
        val trackCountText = resources.getQuantityString(
            com.example.playlistmaker.R.plurals.playlist_track_count,
            tracks.size,
            tracks.size
        )

        val trackWord = trackCountText.replace(Regex("^\\d+\\s*"), "")
        builder.append("\n").append("[").append(tracks.size).append("] ").append(trackWord)
        
        tracks.forEachIndexed { index, track ->
            builder.append("\n")
                .append(index + 1)
                .append(". ")
                .append(track.artistName)
                .append(" - ")
                .append(track.trackName)
                .append(" (")
                .append(track.formattedTrackTime)
                .append(")")
        }
        
        return builder.toString()
    }
}

