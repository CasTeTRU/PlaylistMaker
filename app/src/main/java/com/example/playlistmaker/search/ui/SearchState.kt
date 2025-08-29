package com.example.playlistmaker.search.ui

import com.example.playlistmaker.search.domain.Track

sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    object Error : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
}
