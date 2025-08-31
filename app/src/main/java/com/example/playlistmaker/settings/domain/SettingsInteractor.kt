package com.example.playlistmaker.search.domain

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
}