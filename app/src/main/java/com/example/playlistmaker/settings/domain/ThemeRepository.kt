package com.example.playlistmaker.settings.domain

interface ThemeRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun applyTheme(isDark: Boolean)
}