package com.example.playlistmaker.playlist.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_track_table")
data class PlaylistTrackEntity(
    @PrimaryKey
    val trackId: String,
    val artistName: String,
    val artworkUrl100: String,
    val trackName: String,
    val trackTimeMillis: Long,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val collectionName: String,
    val previewUrl: String
)

