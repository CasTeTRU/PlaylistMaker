package com.example.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.db.FavoriteInteractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val track: Track,
    private val mediaPlayer: MediaPlayer,
    private val favoritesInteractor: FavoriteInteractor
) : ViewModel() {

    companion object { private const val UPDATE_TIME_DELAY_MS = 300L }

    private val _state = MutableLiveData<PlayerState>()
    val state: LiveData<PlayerState> get() = _state

    init {
        _state.value = PlayerState(track = track)
        preparePlayer(track.previewUrl)
    }

    private fun preparePlayer(url: String?) {
        _state.value = _state.value?.copy(isLoading = true)
        url?.let {
            mediaPlayer.setDataSource(it)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                _state.postValue(_state.value?.copy(isLoading = false))
            }
            mediaPlayer.setOnCompletionListener {
                _state.postValue(_state.value?.copy(isPlaying = false, currentPosition = 0))
            }
        }
    }

    fun playbackControl() {
        val currentState = _state.value ?: return
        if (currentState.isPlaying) pausePlayer() else startPlayer()
    }

    private fun startPlayer() {
        _state.value = _state.value?.copy(isPlaying = true)
        mediaPlayer.start()
        updateTimer()
    }

    private fun pausePlayer() {
        _state.value = _state.value?.copy(isPlaying = false)
        mediaPlayer.pause()
    }

    private fun stopPlayer() {
        _state.value = _state.value?.copy(isPlaying = false, currentPosition = 0)
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    fun onPause() { if (_state.value?.isPlaying == true) pausePlayer() }

    fun onStop() { stopPlayer() }

    private fun updateTimer() {
        viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                delay(UPDATE_TIME_DELAY_MS)
                val pos = mediaPlayer.currentPosition.toLong()
                _state.postValue(_state.value?.copy(currentPosition = pos))
            }
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
    }

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            if (!track.isFavorite) {
                favoritesInteractor.addTrack(track)
                track.isFavorite = true
            } else {
                favoritesInteractor.removeTrack(track)
                track.isFavorite = false
            }
            _state.postValue(_state.value?.copy())
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }
}