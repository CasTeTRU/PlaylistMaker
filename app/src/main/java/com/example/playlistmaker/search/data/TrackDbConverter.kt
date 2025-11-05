package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.data.entity.TrackEntity
import com.example.playlistmaker.search.domain.Track

class TrackDbConverter {

    fun map(track: Track): TrackEntity {
        return TrackEntity(
            track.trackId,
            track.artistName,
            track.artworkUrl100,
            track.trackName,
            track.trackTimeMillis,
            track.releaseDate ?: "",
            track.primaryGenreName ?: "",
            track.country ?: "",
            track.collectionName ?: "",
            track.previewUrl ?: ""
        )
    }

    fun map(track: TrackEntity): Track {
        return Track(
            track.trackId,
            track.trackName,
            track.artistName,
            track.trackTimeMillis,
            track.artworkUrl100,
            track.collectionName,
            track.releaseDate,
            track.primaryGenreName,
            track.country,
            track.previewUrl,
            isFavorite = true // Треки из базы данных всегда избранные
        )
    }

}