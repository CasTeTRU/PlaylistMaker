package com.example.playlistmaker.library.ui

import com.example.playlistmaker.search.domain.Track

sealed interface FavoriteStates {

    data class Content(
        val tracks: List<Track>
    ) : FavoriteStates

    object Empty : FavoriteStates
}