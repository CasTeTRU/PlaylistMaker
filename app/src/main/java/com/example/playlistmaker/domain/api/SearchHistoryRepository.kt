package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.TrackDomainModel

interface SearchHistoryRepository {
    fun getHistory(): List<TrackDomainModel>
    fun saveTrack(track: TrackDomainModel)
    fun clearHistory()
} 