package com.example.playlistmaker.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LibraryViewModel : ViewModel() {

    private val _state = MutableLiveData<LibraryState>()
    val state: LiveData<LibraryState> get() = _state

    init {
        _state.value = LibraryState()
    }
}
