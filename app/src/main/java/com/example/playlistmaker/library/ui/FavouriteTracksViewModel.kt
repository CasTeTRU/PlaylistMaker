package com.example.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.ui.FavoriteStates
import com.example.playlistmaker.search.domain.db.FavoriteInteractor
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.launch

class FavoriteViewModel(private val favoritesInteractor: FavoriteInteractor
)
    : ViewModel() {

    private val favoriteLiveData = MutableLiveData<FavoriteStates>()
    fun observeFavorite(): LiveData<FavoriteStates> = favoriteLiveData


    init {
        interactor()
    }

    private fun processResult(tracks: List<Track>) {

        when{
            tracks.isEmpty() -> {
                renderState(
                    FavoriteStates.Empty
                )
            }else -> {
            renderState(
                FavoriteStates.Content(tracks)
            )
        }
        }
    }

    private fun renderState(state: FavoriteStates) {
        favoriteLiveData.postValue(state)
    }

    fun interactor(){
        viewModelScope.launch {
            favoritesInteractor
                .favoriteTracks()
                .collect { tracks ->
                    processResult(tracks)
                }
        }
    }
}

