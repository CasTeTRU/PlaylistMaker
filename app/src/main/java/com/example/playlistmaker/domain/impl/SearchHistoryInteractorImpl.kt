package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.TrackDomainModel

interface SearchHistoryInteractor {
    fun getHistory(): List<TrackDomainModel>
    fun saveTrack(track: TrackDomainModel)
    fun clearHistory()
}

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository) : SearchHistoryInteractor {
    override fun getHistory(): List<TrackDomainModel> = repository.getHistory()
    override fun saveTrack(track: TrackDomainModel) = repository.saveTrack(track)
    override fun clearHistory() = repository.clearHistory()
} 