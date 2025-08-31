package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.TrackRepository
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackDomainModel
import com.example.playlistmaker.search.data.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(private val apiService: ItunesApiService) : TrackRepository {
    override suspend fun searchTracks(query: String): List<TrackDomainModel> = withContext(Dispatchers.IO) {
        val response = apiService.search(query).execute()
        if (response.isSuccessful) {
            response.body()?.results?.map { it.toDomain() } ?: emptyList()
        } else {
            emptyList()
        }
    }
} 