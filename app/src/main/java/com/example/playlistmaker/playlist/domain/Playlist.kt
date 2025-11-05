package com.example.playlistmaker.playlist.domain

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val trackIds: List<String>,
    val trackCount: Int
)

