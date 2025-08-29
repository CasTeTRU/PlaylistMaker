package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.player.domain.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    track: Track
) : ViewModel() {

    companion object {
        private const val UPDATE_TIME_DELAY_MS = 300L
    }

    private val _state = MutableLiveData<PlayerState>()
    val state: LiveData<PlayerState> get() = _state

    private var updateTimeJob: Job? = null

    init {
        _state.value = PlayerState(track = track)
        preparePlayer(track.previewUrl)
    }

    private fun preparePlayer(url: String?) {
        _state.value = _state.value?.copy(isLoading = true)
        
        playerInteractor.preparePlayer(
            url = url,
            onPrepared = {
                _state.value = _state.value?.copy(isLoading = false)
            },
            onCompletion = {
                stopPlayer()
            }
        )
    }

    fun playbackControl() {
        val currentState = _state.value ?: return
        
        if (currentState.isPlaying) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        _state.value = _state.value?.copy(isPlaying = true)
        startUpdateTime()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        _state.value = _state.value?.copy(isPlaying = false)
        stopUpdateTime()
    }

    private fun stopPlayer() {
        playerInteractor.stopPlayer()
        _state.value = _state.value?.copy(
            isPlaying = false,
            currentPosition = 0
        )
        stopUpdateTime()
    }

    fun onPause() {
        if (_state.value?.isPlaying == true) {
            pausePlayer()
        }
    }

    fun onStop() {
        stopPlayer()
    }

    private fun startUpdateTime() {
        updateTimeJob = viewModelScope.launch {
            while (_state.value?.isPlaying == true) {
                val position = playerInteractor.getCurrentPosition()
                _state.value = _state.value?.copy(currentPosition = position)
                delay(UPDATE_TIME_DELAY_MS)
            }
        }
    }

    private fun stopUpdateTime() {
        updateTimeJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        playerInteractor.releasePlayer()
        stopUpdateTime()
    }
}
