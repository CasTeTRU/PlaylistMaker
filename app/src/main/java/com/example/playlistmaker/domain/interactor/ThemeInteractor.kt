package com.example.playlistmaker.domain.interactor

interface ThemeInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun applyTheme(isDark: Boolean)
} 