package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "track"
        private const val CORNER_RADIUS = 8f // Радиус скругления в dp
        private const val UPDATE_TIME_DELAY_MS = 300L // Задержка обновления времени трека
    }

    private lateinit var track: Track
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var playButton: ImageView
    private lateinit var currentTimeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val pos = it.currentPosition
                currentTimeTextView.text = formatTrackTime(pos.toLong())
                if (isPlaying && pos < it.duration) {
                    handler.postDelayed(this, UPDATE_TIME_DELAY_MS)

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        track = intent.getParcelableExtra<Track>(EXTRA_TRACK) ?: run {
            Toast.makeText(this, "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        playButton = findViewById(R.id.playButton)
        currentTimeTextView = findViewById(R.id.currentTimeTextView)

        setupBackButton()
        setupCoverArt()
        setupTrackInfo()
        setupPlaybackControls()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupCoverArt() {
        val imageView = findViewById<ImageView>(R.id.coverArtImageView)
        val placeholderRes = if (isInNightMode()) {
            R.drawable.ic_cover_placeholder_night
        } else {
            R.drawable.ic_cover_placeholder
        }

        val coverUrl = track.getCoverArtwork() ?: run {
            Glide.with(this)
                .load(placeholderRes)
                .into(imageView)
            return
        }

        Glide.with(this)
            .load(coverUrl)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .transform(RoundedCorners(dpToPx(CORNER_RADIUS)))
            .into(imageView)
    }

    private fun dpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.scaledDensity
        return (dp * scale + 0.5f).toInt()
    }

    private fun isInNightMode(): Boolean {
        return (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun formatTrackTime(millis: Long): String {
        return Track.formatMillis(millis)
    }

    private fun setupTrackInfo() {
        findViewById<TextView>(R.id.trackNameTextView).text = track.trackName
        findViewById<TextView>(R.id.artistNameTextView).text = track.artistName

        updateLabelAndValue(R.id.albumLabel, R.id.albumNameTextView, track.collectionName)
        updateLabelAndValue(R.id.yearLabel, R.id.releaseYearTextView, track.getReleaseYear())
        updateLabelAndValue(R.id.genreLabel, R.id.genreTextView, track.primaryGenreName)
        updateLabelAndValue(R.id.countryLabel, R.id.countryTextView, track.country)

        val trackTimeText = if (track.trackTimeMillis > 0) {
            formatTrackTime(track.trackTimeMillis)
        } else {
            "—"
        }
        findViewById<TextView>(R.id.trackTimeTextView).text = trackTimeText
    }

    private fun setupPlaybackControls() {
        playButton.setOnClickListener {
            if (isPlaying) {
                pausePlayer()
            } else {
                startPlayer()
            }
        }
    }

    private fun startPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.previewUrl)
                setOnPreparedListener {
                    start()
                    this@PlayerActivity.isPlaying = true
                    playButton.setImageResource(R.drawable.ic_pause)
                    handler.post(updateTimeRunnable)
                }
                setOnCompletionListener {
                    stopPlayer(resetTime = true)
                }
                prepareAsync()
            }
        } else {
            mediaPlayer?.start()
            isPlaying = true
            playButton.setImageResource(R.drawable.ic_pause)
            handler.post(updateTimeRunnable)
        }
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        isPlaying = false
        playButton.setImageResource(R.drawable.ic_play)
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun stopPlayer(resetTime: Boolean = false) {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
        isPlaying = false
        playButton.setImageResource(R.drawable.ic_play)
        handler.removeCallbacks(updateTimeRunnable)
        if (resetTime) {
            currentTimeTextView.text = formatTrackTime(0)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayer()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateTimeRunnable)
    }


    override fun onBackPressed() {
        stopPlayer(resetTime = true)
        super.onBackPressed()
    }

    private fun updateLabelAndValue(labelId: Int, valueId: Int, value: String?) {
        val labelView = findViewById<TextView>(labelId)
        val valueView = findViewById<TextView>(valueId)

        if (value.isNullOrEmpty()) {
            labelView.visibility = View.GONE
            valueView.visibility = View.GONE
        } else {
            labelView.visibility = View.VISIBLE
            valueView.visibility = View.VISIBLE
            valueView.text = value
        }
    }
}

