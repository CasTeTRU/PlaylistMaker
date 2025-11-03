package com.example.playlistmaker.search.data.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.search.data.toDomain
import com.example.playlistmaker.search.data.toDto
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson = Gson()
) : SearchHistoryRepository {

    private val key = "tracks"
    private val SEARCH_HISTORY_LIST_KEY = "for_search_history_list"
    private val SEARCH_HISTORY_KEY = "for_search_history"


    override fun getHistory(): List<TrackDomainModel> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            val tracks: List<Track> = gson.fromJson(json, type)
            tracks.map { it.toDomain() }
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
        val json = gson.toJson(trimmed.map { track: TrackDomainModel -> track.toDto() })
        prefs.edit().putString(key, json).apply()


        saveTrackToAlternativeFormat(track.toDto())
    }

    override fun clearHistory() {
        prefs.edit().remove(key).apply()

        prefs.edit().remove(SEARCH_HISTORY_LIST_KEY).apply()
        prefs.edit().remove(SEARCH_HISTORY_KEY).apply()
    }
    
    private fun createTracksListFromJson(json: String): MutableList<Track> {
        val trackListType = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, trackListType) ?: mutableListOf()
    }

    private fun createTracksFromJson(json: String): Track {
        val trackType = object : TypeToken<Track>() {}.type
        return gson.fromJson(json, trackType)
    }

    private fun createJsonFromTracksList(tracks: MutableList<Track>): String {
        return gson.toJson(tracks)
    }

    private fun createJsonFromTrack(track: Track): String {
        return gson.toJson(track)
    }

    private fun saveTrackToAlternativeFormat(track: Track) {
        prefs.edit {
            putString(SEARCH_HISTORY_KEY, createJsonFromTrack(track))
        }
    }

    fun getTrack(): Track? {
        val trackString = prefs.getString(SEARCH_HISTORY_KEY, null)
        return trackString?.let { createTracksFromJson(it) }
    }

    fun getTrackList(): MutableList<Track>? {
        val tracksH = prefs.getString(SEARCH_HISTORY_LIST_KEY, null)
        return tracksH?.let { createTracksListFromJson(it) }
    }
}