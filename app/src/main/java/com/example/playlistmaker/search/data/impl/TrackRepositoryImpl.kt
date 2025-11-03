package com.example.playlistmaker.search.data.impl

import android.util.Log
import com.example.playlistmaker.search.data.ItunesApiService
import com.example.playlistmaker.search.data.toDomain
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.example.playlistmaker.search.domain.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(private val apiService: ItunesApiService) : TrackRepository {
    override fun searchTracks(query: String): Flow<List<TrackDomainModel>> = flow {
        try {
            val results = withContext(Dispatchers.IO) {
                val response = apiService.search(query)
                response.results?.map { it.toDomain() } ?: emptyList()
            }
            emit(results)
        } catch (e: Exception) {
            Log.e("TrackRepositoryImpl", "API request failed", e)
            emit(emptyList())
        }
    }
}