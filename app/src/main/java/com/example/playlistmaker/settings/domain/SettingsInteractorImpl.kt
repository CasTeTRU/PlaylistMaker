package com.example.playlistmaker.settings.domain

import com.example.playlistmaker.search.domain.SettingsInteractor

class SettingsInteractorImpl(private val repository: SettingsRepository) : SettingsInteractor {
    override fun isDarkTheme(): Boolean = repository.isDarkTheme()
    override fun setDarkTheme(isDark: Boolean) = repository.setDarkTheme(isDark)
}