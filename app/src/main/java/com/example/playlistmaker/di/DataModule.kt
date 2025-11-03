package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.search.data.ItunesApiService
import com.example.playlistmaker.search.data.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.impl.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.TrackRepository
import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.playlistmaker.search.data.db.AppDatabase
import com.example.playlistmaker.search.data.impl.FavoriteRepositoryImpl
import com.example.playlistmaker.search.domain.db.FavoriteRepository

val dataModule = module {

    single<ItunesApiService> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApiService::class.java)
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        androidContext()
            .getSharedPreferences("playlist_maker_storage", Context.MODE_PRIVATE)
    }

    factory { Gson() }

    single<TrackRepository> {
        TrackRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(), get())
    }

    single<FavoriteRepository> {
        FavoriteRepositoryImpl(get(), get())
    }

}
