package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.Track

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)