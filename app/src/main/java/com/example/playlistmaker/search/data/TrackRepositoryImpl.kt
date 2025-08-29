package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.TrackRepository
import com.example.playlistmaker.search.domain.Track
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

fun Track.toDomain(): TrackDomainModel = TrackDomainModel(
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