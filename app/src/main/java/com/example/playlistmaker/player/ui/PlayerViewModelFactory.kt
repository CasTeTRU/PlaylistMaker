package com.example.playlistmaker.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.player.domain.PlayerInteractor

class PlayerViewModelFactory(
    private val playerInteractor: PlayerInteractor,
    private val track: Track
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(playerInteractor, track) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
