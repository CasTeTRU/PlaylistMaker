package com.example.playlistmaker.player.domain

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.PlayerInteractor

class PlayerInteractorImpl : PlayerInteractor {

    private var mediaPlayer: MediaPlayer? = null

    override fun preparePlayer(url: String?, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        if (url.isNullOrEmpty()) {
            android.util.Log.w("PlayerInteractorImpl", "URL is null or empty")
            return
        }

        android.util.Log.d("PlayerInteractorImpl", "Preparing player with URL: $url")
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                android.util.Log.d("PlayerInteractorImpl", "Player prepared successfully")
                onPrepared()
            }
            setOnCompletionListener {
                android.util.Log.d("PlayerInteractorImpl", "Player completed")
                onCompletion()
            }
            setOnErrorListener { mp, what, extra ->
                android.util.Log.e("PlayerInteractorImpl", "Player error: what=$what, extra=$extra")
                false
            }
            prepareAsync()
        }
    }

    override fun startPlayer() {
        mediaPlayer?.start()
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
    }

    override fun stopPlayer() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
    }

    override fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}