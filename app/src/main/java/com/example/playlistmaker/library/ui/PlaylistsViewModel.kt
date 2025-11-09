package com.example.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class PlaylistsState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false
)

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>()
    val state: LiveData<PlaylistsState> = _state

    val playlistsFlow: Flow<List<Playlist>> = playlistInteractor.getAllPlaylists()

    init {
        observePlaylists()
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistsFlow.collect { playlists ->
                _state.value = PlaylistsState(playlists = playlists)
            }
        }
    }
}
