package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.TrackRepository
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.example.playlistmaker.search.data.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(private val apiService: ItunesApiService) : TrackRepository {
    override suspend fun searchTracks(query: String): List<TrackDomainModel> = withContext(Dispatchers.IO) {
        android.util.Log.d("TrackRepositoryImpl", "Making API request for query: $query")
        val response = apiService.search(query).execute()
        android.util.Log.d("TrackRepositoryImpl", "Response code: ${response.code()}")
        if (response.isSuccessful) {
            val results = response.body()?.results?.map { it.toDomain() } ?: emptyList()
            android.util.Log.d("TrackRepositoryImpl", "Successfully got ${results.size} tracks")
            results
        } else {
            android.util.Log.e("TrackRepositoryImpl", "API request failed: ${response.code()} - ${response.message()}")
            emptyList()
        }
    }
} 