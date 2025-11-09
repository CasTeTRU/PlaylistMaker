package com.example.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchTracksInteractor
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.example.playlistmaker.search.data.toDomain
import com.example.playlistmaker.search.data.toDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val _state = MutableLiveData<SearchState>(SearchState.Initial)
    val state: LiveData<SearchState> get() = _state

    private var searchJob: Job? = null

    init {

        showHistoryIfAvailable()
    }
    fun searchTracks(query: String) {
        if (query.isBlank()) {
            showHistoryIfAvailable()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = SearchState.Loading

            runCatching {
                searchTracksInteractor.searchTracks(query).collect { tracks ->
                    if (tracks.isNotEmpty()) {
                        _state.value = SearchState.Content(tracks.map { it.toDto() })
                    } else {
                        _state.value = SearchState.Empty
                    }
                }
            }.fold(
                onSuccess = { },
                onFailure = { exception ->
                    android.util.Log.e("SearchViewModel", "Error searching tracks", exception)
                    _state.value = SearchState.Error
                }
            )
        }
    }

    fun searchWithDebounce(query: String) {
        if (query.isBlank()) {
            showHistoryIfAvailable()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchTracks(query)
        }
    }

    fun onTrackClick(track: Track) {
        saveTrackToHistory(track)
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()

        showHistoryIfAvailable()
    }

    fun showHistoryIfAvailable() {
        val history = searchHistoryInteractor.getHistory().map { it.toDto() }
        if (history.isNotEmpty()) {
            _state.value = SearchState.History(history)
        } else {
            _state.value = SearchState.Initial
        }
    }

    private fun saveTrackToHistory(track: Track) {
        searchHistoryInteractor.saveTrack(track.toDomain())
    }


    fun addTestHistory() {
        val testTrack = TrackDomainModel(
            trackId = "1",
            trackName = "Test Track",
            artistName = "Test Artist",
            trackTimeMillis = 180000,
            artworkUrl100 = "",
            collectionName = "Test Album",
            releaseDate = "2023",
            primaryGenreName = "Pop",
            country = "USA",
            previewUrl = ""
        )
        searchHistoryInteractor.saveTrack(testTrack)
        showHistoryIfAvailable()
    }
}
