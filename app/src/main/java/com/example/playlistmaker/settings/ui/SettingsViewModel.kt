package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.ThemeInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
    private val themeInteractor: ThemeInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SettingsState>()
    val state: LiveData<SettingsState> get() = _state

    init {
        loadCurrentTheme()
    }

    private fun loadCurrentTheme() {
        val isDarkTheme = themeInteractor.isDarkTheme()
        _state.value = SettingsState(isDarkTheme = isDarkTheme)
    }

    fun toggleTheme(isDarkTheme: Boolean) {
        settingsInteractor.setDarkTheme(isDarkTheme)
        themeInteractor.applyTheme(isDarkTheme)
        _state.value = _state.value?.copy(isDarkTheme = isDarkTheme)
    }
}