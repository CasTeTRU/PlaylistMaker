package com.example.playlistmaker.search.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.search.data.dao.TrackDao
import com.example.playlistmaker.search.data.entity.TrackEntity
import com.example.playlistmaker.playlist.data.dao.PlaylistDao
import com.example.playlistmaker.playlist.data.entity.PlaylistEntity
import com.example.playlistmaker.playlist.data.dao.PlaylistTrackDao
import com.example.playlistmaker.playlist.data.entity.PlaylistTrackEntity

@Database(version = 5, entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao

}