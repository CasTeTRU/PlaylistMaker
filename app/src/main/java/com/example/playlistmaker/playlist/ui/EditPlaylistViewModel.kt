package com.example.playlistmaker.playlist.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistInteractor
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    playlistInteractor: PlaylistInteractor
) : CreatePlaylistViewModel(playlistInteractor) {

    private val _isPlaylistUpdated = MutableLiveData<Boolean>()
    val isPlaylistUpdated: LiveData<Boolean> = _isPlaylistUpdated

    private var playlistId: Long = -1
    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalCoverPath: String? = null

    fun loadPlaylist(playlistId: Long) {
        this.playlistId = playlistId
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            playlist?.let {
                val coverUri = it.coverPath?.let { path ->
                    Uri.fromFile(java.io.File(path))
                }
                originalName = it.name
                originalDescription = it.description ?: ""
                originalCoverPath = it.coverPath
                _state.value = CreatePlaylistState(
                    name = it.name,
                    description = it.description ?: "",
                    coverUri = coverUri,
                    coverPath = it.coverPath,
                    isCreateButtonEnabled = it.name.isNotBlank()
                )
            }
        }
    }

    override fun hasUnsavedChanges(): Boolean {
        val currentState = _state.value ?: return false
        // В режиме редактирования проверяем, были ли изменения
        return currentState.name != originalName ||
                currentState.description != originalDescription ||
                currentState.coverPath != originalCoverPath
    }

    override fun createPlaylist(coverPath: String?) {
        val currentState = _state.value ?: return
        if (currentState.name.isBlank()) return

        viewModelScope.launch {
            try {
                // Используем новый coverPath, если он есть, иначе сохраняем старый
                val finalCoverPath = coverPath ?: originalCoverPath
                val playlist = Playlist(
                    id = playlistId,
                    name = currentState.name,
                    description = currentState.description.takeIf { it.isNotBlank() },
                    coverPath = finalCoverPath,
                    trackIds = emptyList(), // trackIds и trackCount не изменяются при редактировании
                    trackCount = 0
                )
                // Получаем текущий плейлист, чтобы сохранить trackIds и trackCount
                val existingPlaylist = playlistInteractor.getPlaylistById(playlistId)
                existingPlaylist?.let {
                    val updatedPlaylist = playlist.copy(
                        trackIds = it.trackIds,
                        trackCount = it.trackCount
                    )
                    playlistInteractor.updatePlaylist(updatedPlaylist)
                    _isPlaylistUpdated.postValue(true)
                }
            } catch (e: Exception) {
                android.util.Log.e("EditPlaylistViewModel", "Error updating playlist", e)
            }
        }
    }
}

