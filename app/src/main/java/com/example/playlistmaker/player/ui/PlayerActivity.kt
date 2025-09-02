package com.example.playlistmaker.player.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "track"
        private const val CORNER_RADIUS = 8f
    }

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK) ?: run {
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
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.playButton.setOnClickListener {
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
                .into(binding.coverArtImageView)
        } else {
            Glide.with(this)
                .load(placeholderRes)
                .into(binding.coverArtImageView)
        }
    }

    private fun setupTrackInfo(track: Track) {
        binding.trackNameTextView.text = track.trackName
        binding.artistNameTextView.text = track.artistName

        updateLabelAndValue(binding.albumLabel, binding.albumNameTextView, track.collectionName)
        updateLabelAndValue(binding.yearLabel, binding.releaseYearTextView, track.getReleaseYear())
        updateLabelAndValue(binding.genreLabel, binding.genreTextView, track.primaryGenreName)
        updateLabelAndValue(binding.countryLabel, binding.countryTextView, track.country)

        val trackTimeText = if (track.trackTimeMillis > 0) {
            formatTrackTime(track.trackTimeMillis)
        } else {
            "—"
        }
        binding.trackTimeTextView.text = trackTimeText
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
        binding.playButton.setImageResource(iconRes)
    }

    private fun updateCurrentTime(position: Long) {
        binding.currentTimeTextView.text = formatTrackTime(position)
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

    private fun updateLabelAndValue(labelView: android.widget.TextView, valueView: android.widget.TextView, value: String?) {
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
