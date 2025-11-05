package com.example.playlistmaker.playlist.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistInteractor
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val name: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val coverPath: String? = null,
    val isCreateButtonEnabled: Boolean = false
)

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<CreatePlaylistState>()
    val state: LiveData<CreatePlaylistState> = _state

    private val _isPlaylistCreated = MutableLiveData<String?>()
    val isPlaylistCreated: LiveData<String?> = _isPlaylistCreated

    init {
        _state.value = CreatePlaylistState()
    }

    fun updateName(name: String) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(
            name = name,
            isCreateButtonEnabled = name.isNotBlank()
        )
    }

    fun updateDescription(description: String) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(description = description)
    }

    fun updateCoverUri(uri: Uri?) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(coverUri = uri)
    }

    fun updateCoverPath(path: String?) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(coverPath = path)
    }

    fun hasUnsavedChanges(): Boolean {
        val currentState = _state.value ?: return false
        return currentState.name.isNotBlank() ||
                currentState.description.isNotBlank() ||
                currentState.coverUri != null
    }

    fun createPlaylist(coverPath: String?) {
        val currentState = _state.value ?: return
        if (currentState.name.isBlank()) return

        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    name = currentState.name,
                    description = currentState.description.takeIf { it.isNotBlank() },
                    coverPath = coverPath,
                    trackIds = emptyList(),
                    trackCount = 0
                )
                playlistInteractor.createPlaylist(playlist)
                _isPlaylistCreated.postValue(currentState.name)
            } catch (e: Exception) {
                android.util.Log.e("CreatePlaylistViewModel", "Error creating playlist", e)
            }
        }
    }
}

