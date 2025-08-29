package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.data.TrackDomainModel

interface TrackRepository {
    suspend fun searchTracks(query: String): List<TrackDomainModel>
}