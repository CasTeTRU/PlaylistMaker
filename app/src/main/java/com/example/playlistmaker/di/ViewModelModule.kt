package com.example.playlistmaker.di

import com.example.playlistmaker.player.ui.PlayerViewModel
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.SearchViewModel
import com.example.playlistmaker.settings.ui.SettingsViewModel
import com.example.playlistmaker.library.ui.LibraryViewModel
import com.example.playlistmaker.library.ui.PlaylistsViewModel
import com.example.playlistmaker.library.ui.FavoriteViewModel
import com.example.playlistmaker.playlist.ui.CreatePlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel { (track: Track) ->
        PlayerViewModel(track, get(), get(), get())
    }

    viewModel {
        LibraryViewModel()
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        FavoriteViewModel(get())
    }

    viewModel {
        CreatePlaylistViewModel(get())
    }

}
