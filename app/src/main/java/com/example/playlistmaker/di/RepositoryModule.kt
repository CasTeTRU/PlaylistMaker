package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerInteractorImpl
import android.media.MediaPlayer
import com.example.playlistmaker.search.data.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.TrackDbConverter
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.SearchTracksInteractor
import com.example.playlistmaker.search.domain.SearchTracksInteractorImpl
import com.example.playlistmaker.search.domain.SettingsInteractor
import com.example.playlistmaker.search.domain.TrackRepository
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.settings.domain.ThemeInteractor
import com.example.playlistmaker.settings.domain.ThemeInteractorImpl
import com.example.playlistmaker.settings.domain.ThemeRepository
import com.example.playlistmaker.settings.domain.ThemeRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    factory { TrackDbConverter() }

    // MediaPlayer для плеера
    factory { MediaPlayer() }

    // SharedPreferences для хранения настроек
    single<SharedPreferences> { 
        androidContext().getSharedPreferences("playlist_maker_storage", Context.MODE_PRIVATE) 
    }

    single<PlayerInteractor> { PlayerInteractorImpl() }
    
    // Добавляем зависимости для поиска
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get<SharedPreferences>()) }
    single<SearchTracksInteractor> { SearchTracksInteractorImpl(get<TrackRepository>()) }
    
    // Добавляем зависимости для настроек
    single<SettingsRepository> { SettingsRepositoryImpl(get<SharedPreferences>()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get<SharedPreferences>()) }
    single<ThemeInteractor> { ThemeInteractorImpl(get<ThemeRepository>()) }
    single<SettingsInteractor> { SettingsInteractorImpl(get<SettingsRepository>()) }

}