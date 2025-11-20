package com.example.playlistmaker.playlist.domain

import com.example.playlistmaker.search.domain.Track

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): kotlinx.coroutines.flow.Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackResult
    suspend fun getPlaylistTracks(playlistId: Long): List<Track>
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
    suspend fun deletePlaylist(playlistId: Long)
}

