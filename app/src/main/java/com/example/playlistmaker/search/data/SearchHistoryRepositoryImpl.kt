package com.example.playlistmaker.search.data

import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.example.playlistmaker.search.data.toDomain
import com.example.playlistmaker.search.data.toDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(private val prefs: SharedPreferences) : SearchHistoryRepository {
    private val gson = Gson()
    private val key = "tracks"

    override fun getHistory(): List<TrackDomainModel> {
        val json = prefs.getString(key, null)
        android.util.Log.d("SearchHistoryRepository", "getHistory called, json: $json")
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            val tracks: List<Track> = gson.fromJson(json, type)
            android.util.Log.d("SearchHistoryRepository", "Parsed ${tracks.size} tracks from history")
            tracks.map { it.toDomain() }
        } else {
            android.util.Log.d("SearchHistoryRepository", "No history found in SharedPreferences")
            emptyList()
        }
    }

    override fun saveTrack(track: TrackDomainModel) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        val maxSize = 10
        val trimmed = if (history.size > maxSize) history.take(maxSize) else history
        val json = gson.toJson(trimmed.map { track: TrackDomainModel -> track.toDto() })
        android.util.Log.d("SearchHistoryRepository", "Saving track to history: ${track.trackName}, total history size: ${trimmed.size}")
        prefs.edit().putString(key, json).apply()
    }

    override fun clearHistory() {
        android.util.Log.d("SearchHistoryRepository", "Clearing history")
        prefs.edit().remove(key).apply()
    }
} 