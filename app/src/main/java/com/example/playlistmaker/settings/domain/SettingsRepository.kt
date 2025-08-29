package com.example.playlistmaker.settings.domain

interface SettingsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
}