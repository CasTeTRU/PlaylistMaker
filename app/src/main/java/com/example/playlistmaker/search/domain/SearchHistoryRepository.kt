package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.data.TrackDomainModel

interface SearchHistoryRepository {
    fun getHistory(): List<TrackDomainModel>
    fun saveTrack(track: TrackDomainModel)
    fun clearHistory()
}