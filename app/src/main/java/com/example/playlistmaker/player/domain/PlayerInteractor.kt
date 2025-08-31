package com.example.playlistmaker.player.domain

interface PlayerInteractor {
    fun preparePlayer(url: String?, onPrepared: () -> Unit, onCompletion: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun stopPlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Long
    fun isPlaying(): Boolean
}