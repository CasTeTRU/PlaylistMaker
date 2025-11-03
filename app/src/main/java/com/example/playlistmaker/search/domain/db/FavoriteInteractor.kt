package com.example.playlistmaker.search.domain.db

import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteInteractor {

    fun favoriteTracks(): Flow<List<Track>>

    fun addTrack(track: Track)

    fun removeTrack(track: Track)


}