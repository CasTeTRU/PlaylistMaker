package com.example.playlistmaker.search.data.impl

import com.example.playlistmaker.search.data.TrackDbConverter
import com.example.playlistmaker.search.data.db.AppDatabase
import com.example.playlistmaker.search.data.entity.TrackEntity
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.db.FavoriteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class FavoriteRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val trackDbConverter: TrackDbConverter
): FavoriteRepository {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun addTrack(track: Track) {
        scope.launch {
            appDatabase.trackDao().insertTrack(trackDbConverter.map(track))
        }
    }

    override fun removeTrack(track: Track) {
        scope.launch {
            val trackEntity = trackDbConverter.map(track)
            appDatabase.trackDao().deleteTrack(trackEntity)
        }
    }

    override fun getFavorites(): Flow<List<Track>> = flow {
        val tracks = appDatabase.trackDao().getTracks()
        emit(convertFromTrackEntity(tracks))
    }

    private fun convertFromTrackEntity(tracks: List<TrackEntity>): List<Track> {
        return tracks.map { track -> trackDbConverter.map(track) }
    }

}