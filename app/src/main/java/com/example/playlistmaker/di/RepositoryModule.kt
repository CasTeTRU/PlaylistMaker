package com.example.playlistmaker.di

import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerInteractorImpl
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryInteractorImpl
import com.example.playlistmaker.settings.domain.ThemeInteractor
import com.example.playlistmaker.settings.domain.ThemeInteractorImpl
import org.koin.dsl.module

val repositoryModule = module {

    single<PlayerInteractor> { PlayerInteractorImpl() }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }
    
    single<ThemeInteractor> {
        ThemeInteractorImpl(get())
    }

}