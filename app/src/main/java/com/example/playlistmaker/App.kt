package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.di.dataModule
import com.example.playlistmaker.di.interactorModule
import com.example.playlistmaker.di.repositoryModule
import com.example.playlistmaker.di.viewModelModule
import com.example.playlistmaker.settings.domain.ThemeInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class App : Application(), KoinComponent {
    
    private val themeInteractor: ThemeInteractor by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@App)
            modules(dataModule, interactorModule, repositoryModule, viewModelModule)
        }
        
        Creator.appContext = applicationContext

        val isDarkTheme = themeInteractor.isDarkTheme()
        themeInteractor.applyTheme(isDarkTheme)
    }
}