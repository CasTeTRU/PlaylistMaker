package com.example.playlistmaker.player.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.playlist.domain.AddTrackResult
import com.example.playlistmaker.playlist.ui.CreatePlaylistFragment
import com.example.playlistmaker.search.domain.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "track"
        private const val CORNER_RADIUS = 8f
        const val REQUEST_CODE_CREATE_PLAYLIST = 100
    }

    private lateinit var viewModel: PlayerViewModel
    private lateinit var playButton: ImageView
    private lateinit var currentTimeTextView: TextView
    private lateinit var bottomSheetContainer: LinearLayout
    private lateinit var overlay: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter
    private var playlistsJob: Job? = null

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
        setupBottomSheet()
        observeViewModel()
    }

    private fun setupViews() {
        playButton = findViewById(R.id.playButton)
        currentTimeTextView = findViewById(R.id.currentTimeTextView)
        bottomSheetContainer = findViewById(R.id.playlists_bottom_sheet)
        overlay = findViewById(R.id.overlay)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        findViewById<ImageView>(R.id.favoriteButton).setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        findViewById<ImageView>(R.id.addToPlaylistButton).setOnClickListener {
            showBottomSheet()
        }

        overlay.setOnClickListener {
            hideBottomSheet()
        }

        playlistAdapter = PlaylistBottomSheetAdapter { playlist ->
            viewModel.onPlaylistSelected(playlist)
        }

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.playlistsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistAdapter

        findViewById<com.google.android.material.button.MaterialButton>(R.id.newPlaylistButton).setOnClickListener {
            navigateToCreatePlaylist()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                    else -> {
                        overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = kotlin.math.abs(slideOffset)
            }
        })
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        // Обновляем список плейлистов при открытии
        playlistsJob?.cancel()
        playlistsJob = lifecycleScope.launch {
            viewModel.playlistsFlow.collect { playlists ->
                playlistAdapter.submitList(playlists)
            }
        }
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        playlistsJob?.cancel()
        playlistsJob = null
    }

    private fun navigateToCreatePlaylist() {
        hideBottomSheet()
        val fragmentContainer = findViewById<View>(R.id.fragment_container_create_playlist)
        fragmentContainer.visibility = View.VISIBLE
        
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_create_playlist, CreatePlaylistFragment())
                .addToBackStack("create_playlist")
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            android.util.Log.e("PlayerActivity", "Error showing CreatePlaylistFragment", e)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            setupCoverArt(state.track)
            setupTrackInfo(state.track)
            updatePlayButton(state.isPlaying)
            updateCurrentTime(state.currentPosition)
            updateFavoriteButton(state.track.isFavorite)
        }

        viewModel.addTrackResult.observe(this) { result ->
            result?.let {
                when (it) {
                    is AddTrackResult.Success -> {
                        Toast.makeText(
                            this,
                            getString(R.string.added_to_playlist, it.playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                        hideBottomSheet()
                        viewModel.clearAddTrackResult()
                    }
                    is AddTrackResult.AlreadyExists -> {
                        Toast.makeText(
                            this,
                            getString(R.string.track_already_in_playlist, it.playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.clearAddTrackResult()
                    }
                }
            }
        }
    }
    
    private fun updateFavoriteButton(isFavorite: Boolean) {
        val favoriteButton = findViewById<ImageView>(R.id.favoriteButton)
        val iconRes = if (isFavorite) {
            R.drawable.ic_favourite_checked
        } else {
            R.drawable.ic_like_off
        }
        favoriteButton.setImageResource(iconRes)
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

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hideBottomSheet()
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            try {
                supportFragmentManager.popBackStack()
                // Скрываем контейнер после того, как транзакция завершена
                findViewById<View>(R.id.fragment_container_create_playlist).post {
                    findViewById<View>(R.id.fragment_container_create_playlist).visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerActivity", "Error popping back stack", e)
            }
        } else {
            viewModel.onStop()
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }
}
