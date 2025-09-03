package com.example.playlistmaker.settings.domain

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.settings.domain.ThemeRepository

class ThemeRepositoryImpl(private val prefs: SharedPreferences) : ThemeRepository {
    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val TAG = "ThemeRepositoryImpl"
    }

    override fun isDarkTheme(): Boolean {
        val isDark = prefs.getBoolean(KEY_DARK_THEME, false)
        Log.d(TAG, "isDarkTheme: $isDark")
        return isDark
    }

    override fun setDarkTheme(isDark: Boolean) {
        Log.d(TAG, "setDarkTheme: $isDark")
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
    }

    override fun applyTheme(isDark: Boolean) {
        Log.d(TAG, "applyTheme: $isDark")
        val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        Log.d(TAG, "Setting night mode to: $mode")
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}