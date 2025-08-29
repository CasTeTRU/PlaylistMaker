package com.example.playlistmaker.settings.data

import android.content.SharedPreferences
import com.example.playlistmaker.settings.domain.SettingsRepository

class SettingsRepositoryImpl(private val prefs: SharedPreferences) : SettingsRepository {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    override fun isDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
    }
}