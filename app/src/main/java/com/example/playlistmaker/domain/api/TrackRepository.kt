package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.TrackDomainModel

interface TrackRepository {
    suspend fun searchTracks(query: String): List<TrackDomainModel>
} 