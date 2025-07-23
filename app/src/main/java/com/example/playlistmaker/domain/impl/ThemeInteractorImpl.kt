package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.ThemeRepository
import com.example.playlistmaker.domain.interactor.ThemeInteractor

class ThemeInteractorImpl(private val repository: ThemeRepository) : ThemeInteractor {
    override fun isDarkTheme() = repository.isDarkTheme()
    override fun setDarkTheme(isDark: Boolean) = repository.setDarkTheme(isDark)
    override fun applyTheme(isDark: Boolean) = repository.applyTheme(isDark)
} 