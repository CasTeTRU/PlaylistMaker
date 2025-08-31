package com.example.playlistmaker.search.domain

interface TrackRepository {
    suspend fun searchTracks(query: String): List<TrackDomainModel>
}