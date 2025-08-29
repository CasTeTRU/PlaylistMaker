package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.data.TrackDomainModel

interface SearchTracksInteractor {
    suspend fun searchTracks(query: String): List<TrackDomainModel>
}

class SearchTracksInteractorImpl(private val repository: TrackRepository) : SearchTracksInteractor {
    override suspend fun searchTracks(query: String): List<TrackDomainModel> = repository.searchTracks(query)
} 