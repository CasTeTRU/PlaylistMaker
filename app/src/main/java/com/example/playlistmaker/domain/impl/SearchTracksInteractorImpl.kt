package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TrackRepository
import com.example.playlistmaker.domain.models.TrackDomainModel

interface SearchTracksInteractor {
    suspend fun searchTracks(query: String): List<TrackDomainModel>
}

class SearchTracksInteractorImpl(private val repository: TrackRepository) : SearchTracksInteractor {
    override suspend fun searchTracks(query: String): List<TrackDomainModel> = repository.searchTracks(query)
} 