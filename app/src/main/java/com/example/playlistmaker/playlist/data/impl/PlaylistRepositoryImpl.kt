package com.example.playlistmaker.playlist.data.impl

import com.example.playlistmaker.playlist.data.PlaylistConverter
import com.example.playlistmaker.playlist.data.PlaylistTrackConverter
import com.example.playlistmaker.playlist.data.dao.PlaylistDao
import com.example.playlistmaker.playlist.domain.AddTrackResult
import com.example.playlistmaker.playlist.domain.Playlist
import com.example.playlistmaker.playlist.domain.PlaylistRepository
import com.example.playlistmaker.search.data.db.AppDatabase
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistConverter: PlaylistConverter,
    private val playlistTrackConverter: PlaylistTrackConverter
) : PlaylistRepository {

    private val playlistDao: PlaylistDao = appDatabase.playlistDao()
    private val playlistTrackDao = appDatabase.playlistTrackDao()

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = playlistConverter.map(playlist)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = playlistConverter.map(playlist)
        playlistDao.updatePlaylist(entity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            playlistConverter.map(entities)
        }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(id) ?: return null
        return playlistConverter.map(entity)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackResult {
        // Получаем плейлист по id
        val playlist = getPlaylistById(playlistId) ?: return AddTrackResult.AlreadyExists("")
        
        // Проверяем, есть ли уже трек в плейлисте
        if (playlist.trackIds.contains(track.trackId)) {
            return AddTrackResult.AlreadyExists(playlist.name)
        }

        // Добавляем trackId в список
        val updatedTrackIds = playlist.trackIds + track.trackId
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = playlist.trackCount + 1
        )

        // Обновляем плейлист в БД
        updatePlaylist(updatedPlaylist)

        // Сохраняем трек в таблицу треков плейлистов
        val trackEntity = playlistTrackConverter.map(track)
        playlistTrackDao.insertTrack(trackEntity)

        return AddTrackResult.Success(playlist.name)
    }

    override suspend fun getPlaylistTracks(playlistId: Long): List<Track> {
        val playlist = getPlaylistById(playlistId) ?: return emptyList()
        if (playlist.trackIds.isEmpty()) return emptyList()
        
        // Получаем все треки из таблицы плейлистов
        val allTracks = playlistTrackDao.getAllTracks()
        // Создаем Map для быстрого поиска треков по trackId
        val tracksMap = allTracks.associateBy { it.trackId }
        
        // Сортируем треки по порядку в playlist.trackIds и разворачиваем список,
        // чтобы последние добавленные треки были вверху
        val sortedTracks = playlist.trackIds
            .mapNotNull { tracksMap[it] }
            .reversed() // Разворачиваем список - последние добавленные вверху
            .map { playlistTrackConverter.map(it) }
        
        return sortedTracks
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        val playlist = getPlaylistById(playlistId) ?: return
        
        // Удаляем trackId из списка
        val updatedTrackIds = playlist.trackIds.filter { it != trackId }
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = playlist.trackCount - 1
        )
        
        // Обновляем плейлист в БД
        updatePlaylist(updatedPlaylist)
        
        // Проверяем, используется ли трек в других плейлистах
        checkAndRemoveUnusedTrack(trackId)
    }


    private suspend fun checkAndRemoveUnusedTrack(trackId: String) {
        // Получаем все плейлисты
        val allPlaylistsEntities = playlistDao.getAllPlaylists().first()
        val allPlaylistsDomain = playlistConverter.map(allPlaylistsEntities)
        
        // Проверяем, есть ли трек хотя бы в одном плейлисте
        val isTrackUsed = allPlaylistsDomain.any { playlist ->
            playlist.trackIds.contains(trackId)
        }
        
        // Если трек не используется ни в одном плейлисте, удаляем его из таблицы
        if (!isTrackUsed) {
            playlistTrackDao.deleteTrack(trackId)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = getPlaylistById(playlistId) ?: return
        
        // Удаляем все треки плейлиста из таблицы, если они не используются в других плейлистах
        playlist.trackIds.forEach { trackId ->
            checkAndRemoveUnusedTrack(trackId)
        }
        
        // Удаляем плейлист из БД
        playlistDao.deletePlaylist(playlistId)
    }
}

