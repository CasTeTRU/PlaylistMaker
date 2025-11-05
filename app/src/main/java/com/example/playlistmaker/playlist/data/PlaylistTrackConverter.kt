package com.example.playlistmaker.playlist.data

import com.example.playlistmaker.playlist.data.entity.PlaylistTrackEntity
import com.example.playlistmaker.search.domain.Track

class PlaylistTrackConverter {
    fun map(track: Track): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = track.trackId,
            artistName = track.artistName,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            trackTimeMillis = track.trackTimeMillis,
            releaseDate = track.releaseDate ?: "",
            primaryGenreName = track.primaryGenreName ?: "",
            country = track.country ?: "",
            collectionName = track.collectionName ?: "",
            previewUrl = track.previewUrl ?: ""
        )
    }
}

