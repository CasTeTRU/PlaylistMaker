package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.creator.Creator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Creator.appContext = applicationContext
        val themeInteractor = Creator.provideThemeInteractor()
        themeInteractor.applyTheme(themeInteractor.isDarkTheme())
    }
}