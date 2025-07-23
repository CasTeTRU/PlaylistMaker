package com.example.playlistmaker.data.repository

import android.content.Context
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.TrackDomainModel
import com.example.playlistmaker.data.dto.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(private val context: Context) : SearchHistoryRepository {
    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "tracks"

    override fun getHistory(): List<TrackDomainModel> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(json, type).map { it.toDomain() }
        } else {
            emptyList()
        }
    }

    override fun saveTrack(track: TrackDomainModel) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        val maxSize = 10
        val trimmed = if (history.size > maxSize) history.take(maxSize) else history
        val json = gson.toJson(trimmed.map { it.toDto() })
        prefs.edit().putString(key, json).apply()
    }

    override fun clearHistory() {
        prefs.edit().remove(key).apply()
    }
}

fun TrackDomainModel.toDto(): Track = Track(
    trackId = this.trackId,
    trackName = this.trackName,
    artistName = this.artistName,
    trackTimeMillis = this.trackTimeMillis,
    artworkUrl100 = this.artworkUrl100,
    collectionName = this.collectionName,
    releaseDate = this.releaseDate,
    primaryGenreName = this.primaryGenreName,
    country = this.country,
    previewUrl = this.previewUrl
) 