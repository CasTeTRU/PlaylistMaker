package com.example.playlistmaker.domain.api

interface ThemeRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun applyTheme(isDark: Boolean)
} 