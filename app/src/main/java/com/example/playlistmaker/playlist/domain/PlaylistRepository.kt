package com.example.playlistmaker.playlist.domain

import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

sealed class AddTrackResult {
    data class Success(val playlistName: String) : AddTrackResult()
    data class AlreadyExists(val playlistName: String) : AddTrackResult()
}

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackResult
    suspend fun getPlaylistTracks(playlistId: Long): List<Track>
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
    suspend fun deletePlaylist(playlistId: Long)
}

