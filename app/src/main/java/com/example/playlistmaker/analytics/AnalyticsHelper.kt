package com.example.playlistmaker.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {
    private val firebaseAnalytics = Firebase.analytics

    fun logAddToFavorites(trackName: String, artistName: String, trackId: String) {
        val bundle = Bundle().apply {
            putString("track_name", trackName)
            putString("artist_name", artistName)
            putString("track_id", trackId)
        }
        firebaseAnalytics.logEvent("add_to_favorites", bundle)
    }
}

