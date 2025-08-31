package com.example.playlistmaker.search.domain

interface SearchHistoryRepository {
    fun getHistory(): List<TrackDomainModel>
    fun saveTrack(track: TrackDomainModel)
    fun clearHistory()
}