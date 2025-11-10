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

    fun map(entity: PlaylistTrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTimeMillis = entity.trackTimeMillis,
            artworkUrl100 = entity.artworkUrl100,
            collectionName = entity.collectionName.takeIf { it.isNotEmpty() },
            releaseDate = entity.releaseDate.takeIf { it.isNotEmpty() },
            primaryGenreName = entity.primaryGenreName.takeIf { it.isNotEmpty() },
            country = entity.country.takeIf { it.isNotEmpty() },
            previewUrl = entity.previewUrl.takeIf { it.isNotEmpty() },
            isFavorite = false
        )
    }
}

