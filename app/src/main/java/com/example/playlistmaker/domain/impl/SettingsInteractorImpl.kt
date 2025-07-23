package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.interactor.SettingsInteractor

class SettingsInteractorImpl(private val repository: SettingsRepository) : SettingsInteractor {
    override fun isDarkTheme(): Boolean = repository.isDarkTheme()
    override fun setDarkTheme(isDark: Boolean) = repository.setDarkTheme(isDark)
} 