package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.db.FavoriteInteractor
import com.example.playlistmaker.search.domain.db.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteInteractorImpl(private val favoriteRepository: FavoriteRepository):
    FavoriteInteractor {

    override fun favoriteTracks(): Flow<List<Track>> {
        return favoriteRepository.getFavorites().map { tracks -> tracks.reversed() }
    }

    override fun addTrack(track: Track){
        favoriteRepository.addTrack(track)
    }

    override fun removeTrack(track: Track) {
        favoriteRepository.removeTrack(track)
    }
}