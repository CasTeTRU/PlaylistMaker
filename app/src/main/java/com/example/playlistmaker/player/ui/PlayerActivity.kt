package com.example.playlistmaker.player.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "track"
        private const val CORNER_RADIUS = 8f
    }

    private lateinit var viewModel: PlayerViewModel
    private lateinit var playButton: ImageView
    private lateinit var currentTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK) ?: run {
            android.util.Log.e("PlayerActivity", "No track found in intent")
            Toast.makeText(this, "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val viewModelInstance: PlayerViewModel by viewModel { parametersOf(track) }
        viewModel = viewModelInstance

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        playButton = findViewById(R.id.playButton)
        currentTimeTextView = findViewById(R.id.currentTimeTextView)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            setupCoverArt(state.track)
            setupTrackInfo(state.track)
            updatePlayButton(state.isPlaying)
            updateCurrentTime(state.currentPosition)
        }
    }

    private fun setupCoverArt(track: Track) {
        val imageView = findViewById<ImageView>(R.id.coverArtImageView)
        val placeholderRes = if (isInNightMode()) {
            R.drawable.ic_cover_placeholder_night
        } else {
            R.drawable.ic_cover_placeholder
        }

        val coverUrl = track.getCoverArtwork()

        if (coverUrl != null) {
            Glide.with(this)
                .load(coverUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .transform(RoundedCorners(dpToPx(CORNER_RADIUS)))
                .into(imageView)
        } else {
            Glide.with(this)
                .load(placeholderRes)
                .into(imageView)
        }
    }

    private fun setupTrackInfo(track: Track) {
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

    private fun updatePlayButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
        playButton.setImageResource(iconRes)
    }

    private fun updateCurrentTime(position: Long) {
        currentTimeTextView.text = formatTrackTime(position)
    }

    private fun formatTrackTime(millis: Long): String {
        return Track.Companion.formatMillis(millis)
    }

    private fun dpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.scaledDensity
        return (dp * scale + 0.5f).toInt()
    }

    private fun isInNightMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
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

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onBackPressed() {
        viewModel.onStop()
        super.onBackPressed()
    }
}
