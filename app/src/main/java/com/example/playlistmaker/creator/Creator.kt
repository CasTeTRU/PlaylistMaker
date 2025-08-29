package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.search.data.NetworkClient
import com.example.playlistmaker.search.data.SearchHistoryRepositoryImpl
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.ThemeRepositoryImpl
import com.example.playlistmaker.search.data.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.settings.domain.ThemeRepository
import com.example.playlistmaker.search.domain.TrackRepository
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.SearchTracksInteractor
import com.example.playlistmaker.search.domain.SearchTracksInteractorImpl
import com.example.playlistmaker.settings.domain.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.ThemeInteractorImpl
import com.example.playlistmaker.player.domain.PlayerInteractorImpl
import com.example.playlistmaker.search.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.ThemeInteractor
import com.example.playlistmaker.player.domain.PlayerInteractor

object Creator {
    lateinit var appContext: Context

    fun provideTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(NetworkClient.itunesApi)
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(prefs)
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        return SettingsRepositoryImpl(prefs)
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        return SearchTracksInteractorImpl(provideTrackRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(provideSearchHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(provideSettingsRepository(context))
    }

    fun provideThemeRepository(): ThemeRepository {
        val prefs = appContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        return ThemeRepositoryImpl(prefs)
    }

    fun provideThemeInteractor(): ThemeInteractor {
        return ThemeInteractorImpl(provideThemeRepository())
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl()
    }
}