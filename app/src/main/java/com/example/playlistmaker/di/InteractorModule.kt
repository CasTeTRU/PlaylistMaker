package com.example.playlistmaker.di

import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerInteractorImpl
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.domain.SearchTracksInteractor
import com.example.playlistmaker.search.domain.SearchTracksInteractorImpl
import com.example.playlistmaker.search.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.ThemeInteractor
import com.example.playlistmaker.settings.domain.ThemeInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    single<SearchTracksInteractor> {
        SearchTracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    single<ThemeInteractor> {
        ThemeInteractorImpl(get())
    }

    single<PlayerInteractor> {
        PlayerInteractorImpl()
    }

}
