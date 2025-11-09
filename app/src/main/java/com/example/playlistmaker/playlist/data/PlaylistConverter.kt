package com.example.playlistmaker.playlist.data

import com.example.playlistmaker.playlist.data.entity.PlaylistEntity
import com.example.playlistmaker.playlist.domain.Playlist
import com.google.gson.Gson

class PlaylistConverter(private val gson: Gson) {

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIds = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
    }

    fun map(playlistEntity: PlaylistEntity): Playlist {
        val trackIds = try {
            if (playlistEntity.trackIds.isBlank()) {
                emptyList()
            } else {
                gson.fromJson(playlistEntity.trackIds, Array<String>::class.java)?.toList() ?: emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistConverter", "Error parsing trackIds", e)
            emptyList()
        }
        
        return Playlist(
            id = playlistEntity.id,
            name = playlistEntity.name,
            description = playlistEntity.description,
            coverPath = playlistEntity.coverPath,
            trackIds = trackIds,
            trackCount = playlistEntity.trackCount
        )
    }

    fun map(playlistEntities: List<PlaylistEntity>): List<Playlist> {
        return playlistEntities.map { map(it) }
    }
}

