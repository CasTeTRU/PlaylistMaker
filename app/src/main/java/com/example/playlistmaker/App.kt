package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.data.dto.ThemeManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val themeManager = ThemeManager(this)
        themeManager.applyTheme(themeManager.isDarkTheme())
    }
}