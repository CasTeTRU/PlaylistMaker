package com.example.playlistmaker.playlist.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val trackIds: List<String>,
    val trackCount: Int
) : Parcelable

