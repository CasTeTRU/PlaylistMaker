package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface SearchTracksInteractor {
    fun searchTracks(query: String): Flow<List<TrackDomainModel>>
}

class SearchTracksInteractorImpl(private val repository: TrackRepository) : SearchTracksInteractor {
    override fun searchTracks(query: String): Flow<List<TrackDomainModel>> = repository.searchTracks(query)
} 