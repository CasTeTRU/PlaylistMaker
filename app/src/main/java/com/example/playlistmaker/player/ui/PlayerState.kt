package com.example.playlistmaker.player.ui

import com.example.playlistmaker.search.domain.Track

data class PlayerState(
    val track: Track,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
