package com.example.playlistmaker.settings.domain

interface ThemeInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun applyTheme(isDark: Boolean)
}