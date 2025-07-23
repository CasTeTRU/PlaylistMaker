package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.api.SettingsRepository
import com.example.playlistmaker.domain.api.TrackRepository
import com.example.playlistmaker.domain.impl.SearchHistoryInteractor
import com.example.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.impl.SearchTracksInteractor
import com.example.playlistmaker.domain.impl.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.impl.SettingsInteractorImpl
import com.example.playlistmaker.data.network.NetworkClient

object Creator {
    fun provideTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(NetworkClient.itunesApi)
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(context)
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
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
} 