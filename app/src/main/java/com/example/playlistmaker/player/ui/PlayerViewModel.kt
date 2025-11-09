package com.example.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlist.domain.AddTrackResult
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.db.FavoriteInteractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class PlayerViewModel(
    private val track: Track,
    private val mediaPlayer: MediaPlayer,
    private val favoritesInteractor: FavoriteInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    companion object { private const val UPDATE_TIME_DELAY_MS = 300L }

    private val _state = MutableLiveData<PlayerState>()
    val state: LiveData<PlayerState> get() = _state
    
    val playlistsFlow: Flow<List<Playlist>> = playlistInteractor.getAllPlaylists()

    private val _addTrackResult = MutableLiveData<AddTrackResult?>()
    val addTrackResult: LiveData<AddTrackResult?> = _addTrackResult
    
    private var isPlayerPrepared = false
    private var shouldStartWhenPrepared = false
    private var updateTimeJob: Job? = null

    init {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isTrackFavorite(track.trackId)
            val trackWithFavoriteStatus = track.copy(isFavorite = isFavorite)
            _state.value = PlayerState(track = trackWithFavoriteStatus)
            preparePlayer(track.previewUrl)
        }
    }

    private fun preparePlayer(url: String?) {
        _state.value = _state.value?.copy(isLoading = true)
        isPlayerPrepared = false
        url?.let {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(it)
                mediaPlayer.setOnPreparedListener { mp ->
                    isPlayerPrepared = true
                    _state.postValue(_state.value?.copy(isLoading = false))
                    android.util.Log.d("PlayerViewModel", "MediaPlayer prepared, shouldStart=$shouldStartWhenPrepared")
                    if (shouldStartWhenPrepared) {
                        shouldStartWhenPrepared = false
                        startPlayer()
                    }
                }
                mediaPlayer.setOnErrorListener { _, what, extra ->
                    android.util.Log.e("PlayerViewModel", "MediaPlayer error: what=$what, extra=$extra")
                    isPlayerPrepared = false
                    shouldStartWhenPrepared = false
                    _state.postValue(_state.value?.copy(isLoading = false, errorMessage = "Ошибка воспроизведения"))
                    false
                }
                mediaPlayer.setOnCompletionListener {
                    _state.postValue(_state.value?.copy(isPlaying = false, currentPosition = 0))
                }
                mediaPlayer.prepareAsync()
                android.util.Log.d("PlayerViewModel", "Started preparing MediaPlayer with URL: $it")
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Error preparing player", e)
                isPlayerPrepared = false
                shouldStartWhenPrepared = false
                _state.postValue(_state.value?.copy(isLoading = false, errorMessage = "Ошибка загрузки: ${e.message}"))
            }
        } ?: run {
            android.util.Log.e("PlayerViewModel", "Preview URL is null")
            _state.postValue(_state.value?.copy(isLoading = false, errorMessage = "URL трека недоступен"))
        }
    }

    fun playbackControl() {
        val currentState = _state.value ?: return
        if (currentState.isPlaying) {
            pausePlayer()
        } else {
            if (isPlayerPrepared) {
                startPlayer()
            } else {
                android.util.Log.d("PlayerViewModel", "Player not ready yet, will start when prepared")
                shouldStartWhenPrepared = true
            }
        }
    }

    private fun startPlayer() {
        try {
            if (isPlayerPrepared) {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    android.util.Log.d("PlayerViewModel", "MediaPlayer.start() called, isPlaying=${mediaPlayer.isPlaying}")
                }
                val currentState = _state.value
                _state.postValue(currentState?.copy(isPlaying = true))
                android.util.Log.d("PlayerViewModel", "State updated: isPlaying=true")
                updateTimer()
            } else {
                android.util.Log.w("PlayerViewModel", "Cannot start: player not prepared")
            }
        } catch (e: Exception) {
            android.util.Log.e("PlayerViewModel", "Error starting player", e)
            _state.postValue(_state.value?.copy(isPlaying = false, errorMessage = "Ошибка воспроизведения: ${e.message}"))
        }
    }

    private fun pausePlayer() {
        try {
            mediaPlayer.pause()
            val currentState = _state.value
            _state.postValue(currentState?.copy(isPlaying = false))
            android.util.Log.d("PlayerViewModel", "MediaPlayer paused")
        } catch (e: Exception) {
            android.util.Log.e("PlayerViewModel", "Error pausing player", e)
        }
    }

    private fun stopPlayer() {
        try {
            mediaPlayer.stop()
            mediaPlayer.reset()
            val currentState = _state.value
            _state.postValue(currentState?.copy(isPlaying = false, currentPosition = 0))
        } catch (e: Exception) {
            android.util.Log.e("PlayerViewModel", "Error stopping player", e)
        }
    }

    fun onPause() { if (_state.value?.isPlaying == true) pausePlayer() }

    fun onStop() { stopPlayer() }

    private fun updateTimer() {
        updateTimeJob?.cancel()
        updateTimeJob = viewModelScope.launch {
            while (true) {
                delay(UPDATE_TIME_DELAY_MS)
                val isPlaying = mediaPlayer.isPlaying
                val currentState = _state.value
                
                if (isPlaying) {
                    val pos = mediaPlayer.currentPosition.toLong()
                    _state.postValue(currentState?.copy(isPlaying = true, currentPosition = pos))
                } else {
                    // Если MediaPlayer перестал играть, обновляем состояние
                    if (currentState?.isPlaying == true) {
                        _state.postValue(currentState.copy(isPlaying = false))
                    }
                    break
                }
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val currentState = _state.value ?: return@launch
            val track = currentState.track
            val updatedTrack = track.copy(isFavorite = !track.isFavorite)
            
            if (updatedTrack.isFavorite) {
                favoritesInteractor.addTrack(updatedTrack)
            } else {
                favoritesInteractor.removeTrack(updatedTrack)
            }
            
            _state.postValue(currentState.copy(track = updatedTrack))
        }
    }

    fun onPlaylistSelected(playlist: Playlist) {
        viewModelScope.launch {
            // Проверяем, есть ли трек уже в плейлисте
            if (playlist.trackIds.contains(track.trackId)) {
                _addTrackResult.postValue(AddTrackResult.AlreadyExists(playlist.name))
            } else {
                // Добавляем трек через интерактор, передавая только id плейлиста
                val result = playlistInteractor.addTrackToPlaylist(playlist.id, track)
                _addTrackResult.postValue(result)
            }
        }
    }

    fun clearAddTrackResult() {
        _addTrackResult.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        updateTimeJob?.cancel()
        mediaPlayer.release()
    }
}