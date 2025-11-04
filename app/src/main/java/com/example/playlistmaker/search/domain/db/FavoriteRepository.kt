package com.example.playlistmaker.search.domain.db

import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    fun addTrack(track: Track)

    fun removeTrack(track: Track)

    fun getFavorites(): Flow<List<Track>>
    
    suspend fun isTrackFavorite(trackId: String): Boolean
}