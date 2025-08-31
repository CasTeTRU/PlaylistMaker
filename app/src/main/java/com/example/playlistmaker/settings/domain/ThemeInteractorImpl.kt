package com.example.playlistmaker.settings.domain

import com.example.playlistmaker.settings.domain.ThemeInteractor

class ThemeInteractorImpl(private val repository: ThemeRepository) : ThemeInteractor {
    override fun isDarkTheme() = repository.isDarkTheme()
    override fun setDarkTheme(isDark: Boolean) = repository.setDarkTheme(isDark)
    override fun applyTheme(isDark: Boolean) = repository.applyTheme(isDark)
}