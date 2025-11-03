package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun searchTracks(query: String): Flow<List<TrackDomainModel>>
}