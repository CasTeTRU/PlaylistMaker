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

open class CreatePlaylistViewModel(
    protected val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    protected val _state = MutableLiveData<CreatePlaylistState>()
    val state: LiveData<CreatePlaylistState> = _state

    protected val _isPlaylistCreated = MutableLiveData<String?>()
    val isPlaylistCreated: LiveData<String?> = _isPlaylistCreated

    private val _isPlaylistUpdated = MutableLiveData<Boolean>()
    val isPlaylistUpdated: LiveData<Boolean> = _isPlaylistUpdated

    private var editingPlaylist: Playlist? = null
    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalCoverPath: String? = null

    init {
        _state.value = CreatePlaylistState()
    }

    fun initializePlaylist(playlist: Playlist?) {
        editingPlaylist = playlist
        if (playlist != null) {
            val coverUri = playlist.coverPath?.let { path ->
                Uri.fromFile(java.io.File(path))
            }
            originalName = playlist.name
            originalDescription = playlist.description ?: ""
            originalCoverPath = playlist.coverPath
            _state.value = CreatePlaylistState(
                name = playlist.name,
                description = playlist.description ?: "",
                coverUri = coverUri,
                coverPath = playlist.coverPath,
                isCreateButtonEnabled = playlist.name.isNotBlank()
            )
        }
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

    open fun hasUnsavedChanges(): Boolean {
        val currentState = _state.value ?: return false
        if (editingPlaylist != null) {
            return currentState.name != originalName ||
                    currentState.description != originalDescription ||
                    currentState.coverPath != originalCoverPath
        }

        return currentState.name.isNotBlank() ||
                currentState.description.isNotBlank() ||
                currentState.coverUri != null
    }

    open fun createPlaylist(coverPath: String?) {
        val currentState = _state.value ?: return
        if (currentState.name.isBlank()) return

        viewModelScope.launch {
            try {
                if (editingPlaylist != null) {
                    val finalCoverPath = coverPath ?: originalCoverPath
                    val existingPlaylist = editingPlaylist!!
                    val updatedPlaylist = Playlist(
                        id = existingPlaylist.id,
                        name = currentState.name,
                        description = currentState.description.takeIf { it.isNotBlank() },
                        coverPath = finalCoverPath,
                        trackIds = existingPlaylist.trackIds,
                        trackCount = existingPlaylist.trackCount
                    )
                    playlistInteractor.updatePlaylist(updatedPlaylist)
                    _isPlaylistUpdated.postValue(true)
                } else {

                    val playlist = Playlist(
                        name = currentState.name,
                        description = currentState.description.takeIf { it.isNotBlank() },
                        coverPath = coverPath,
                        trackIds = emptyList(),
                        trackCount = 0
                    )
                    playlistInteractor.createPlaylist(playlist)
                    _isPlaylistCreated.postValue(currentState.name)
                }
            } catch (e: Exception) {
                android.util.Log.e("CreatePlaylistViewModel", "Error saving playlist", e)
            }
        }
    }
}

