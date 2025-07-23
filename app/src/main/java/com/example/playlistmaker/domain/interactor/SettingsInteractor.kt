package com.example.playlistmaker.domain.interactor

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
} 