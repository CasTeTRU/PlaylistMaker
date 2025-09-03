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

    private val _themeChanged = MutableLiveData<Boolean>()
    val themeChanged: LiveData<Boolean> get() = _themeChanged

    init {
        loadCurrentTheme()
    }

    private fun loadCurrentTheme() {
        val isDarkTheme = themeInteractor.isDarkTheme()
        android.util.Log.d("SettingsViewModel", "loadCurrentTheme: isDarkTheme = $isDarkTheme")
        _state.value = SettingsState(isDarkTheme = isDarkTheme)
    }

    fun toggleTheme(isDarkTheme: Boolean) {
        try {
            android.util.Log.d("SettingsViewModel", "toggleTheme called with isDarkTheme: $isDarkTheme")
            settingsInteractor.setDarkTheme(isDarkTheme)
            android.util.Log.d("SettingsViewModel", "Theme saved to settings")
            themeInteractor.applyTheme(isDarkTheme)
            android.util.Log.d("SettingsViewModel", "Theme applied")
            _state.value = _state.value?.copy(isDarkTheme = isDarkTheme)
            _themeChanged.value = isDarkTheme
            
            // Уведомляем об изменении темы
            _themeChanged.value = true
            android.util.Log.d("SettingsViewModel", "Theme toggle completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error toggling theme", e)
            // В случае ошибки возвращаем предыдущее состояние
            _state.value = _state.value?.copy(isDarkTheme = !isDarkTheme)
        }
    }
}