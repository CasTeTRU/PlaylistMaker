package com.example.playlistmaker.ui.search

import com.example.playlistmaker.presentation.TrackAdapter
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.data.dto.Track
import com.example.playlistmaker.domain.impl.SearchTracksInteractor
import com.example.playlistmaker.domain.impl.SearchHistoryInteractor
import com.example.playlistmaker.domain.models.TrackDomainModel
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.player.PlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val MAX_HISTORY_SIZE = 10
private const val HISTORY_KEY = "history_tracks"

class SearchActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var placeholderStub: ViewStub
    private lateinit var errorStub: ViewStub
    private var errorLayout: View? = null
    private var emptyLayout: View? = null
    private val sharedPreferences by lazy {
        getSharedPreferences("search_history", MODE_PRIVATE)
    }
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var progressBar: ProgressBar
    private var isClickAllowed = true
    private val clickDebounceDelay = 1000L // 1 секунда
    private val clickHandler = Handler(Looper.getMainLooper())
    private lateinit var searchTracksInteractor: SearchTracksInteractor
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    private fun debounceClick(action: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            action()
            clickHandler.postDelayed({ isClickAllowed = true }, clickDebounceDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.tracksRecyclerView)
        placeholderStub = findViewById(R.id.placeholder_stub)
        errorStub = findViewById(R.id.error_stub)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        progressBar = findViewById(R.id.progressBar)

        searchTracksInteractor = Creator.provideSearchTracksInteractor()
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)

        trackAdapter = TrackAdapter(emptyList()) { track ->
            debounceClick {
                saveTrackToHistory(track)
                val intent = Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                }
                startActivity(intent)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        showHistoryIfAvailable()

        findViewById<Toolbar>(R.id.search_header).setNavigationOnClickListener { finish() }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
            trackAdapter.updateData(emptyList())
            hideAllPlaceholders()
            showHistoryIfAvailable()
        }

        clearHistoryButton.setOnClickListener {
            clearHistory()
            historyTitle.isVisible = false
            clearHistoryButton.isVisible = false
            trackAdapter.updateData(emptyList())
            recyclerView.isVisible = false
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchTracks(query)
                }
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.isVisible = s?.isNotEmpty() == true
                showHistoryIfAvailable()

                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    searchRunnable = Runnable {
                        progressBar.visibility = View.VISIBLE

                        searchTracks(query)

                    }
                    searchHandler.postDelayed(searchRunnable!!, 2000)
                } else {
                    trackAdapter.updateData(emptyList())

                    recyclerView.isVisible = false
                    progressBar.visibility = View.GONE

                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
                showHistoryIfAvailable()
            }
        }

        searchEditText.requestFocus()
        showKeyboard()
    }

    private fun showHistoryIfAvailable() {
        val history = loadHistoryTracks()
        if (searchEditText.text.isEmpty() && searchEditText.hasFocus() && history.isNotEmpty()) {
            trackAdapter.updateData(history)
            recyclerView.visibility = View.VISIBLE
            historyTitle.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            hideAllPlaceholders()
        } else {
            historyTitle.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun searchTracks(query: String) {

        if (!isNetworkAvailable()) {
            progressBar.visibility = View.GONE
            showErrorPlaceholder()
            return
        }

        hideAllPlaceholders()

        progressBar.visibility = View.VISIBLE

        uiScope.launch {
            val tracks = searchTracksInteractor.searchTracks(query)
            progressBar.visibility = View.GONE
            if (tracks.isNotEmpty()) {
                trackAdapter.updateData(tracks.map { it.toDto() })
                recyclerView.visibility = View.VISIBLE
            } else {
                showEmptyPlaceholder()
            }
        }
    }

    private fun showEmptyPlaceholder() {
        hideAllPlaceholders()
        if (emptyLayout == null) {
            emptyLayout = placeholderStub.inflate()
        }
        emptyLayout?.visibility = View.VISIBLE
    }

    private fun showErrorPlaceholder() {
        hideAllPlaceholders()
        if (errorLayout == null) {
            errorLayout = errorStub.inflate()
            val retryBtn = errorLayout?.findViewById<Button>(R.id.retryButton)
            retryBtn?.setOnClickListener {
                val query = searchEditText.text.toString().trim()
                if (!isNetworkAvailable()) {
                    Toast.makeText(
                        this,
                        getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (query.isNotEmpty()) {
                    searchTracks(query)
                }
            }
        }
        errorLayout?.visibility = View.VISIBLE
    }

    private fun hideAllPlaceholders() {
        emptyLayout?.visibility = View.GONE
        errorLayout?.visibility = View.GONE
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        searchEditText.requestFocus()
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (searchEditText.isFocused) {
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    fun saveTrackToHistory(track: Track) {
        searchHistoryInteractor.saveTrack(track.toDomain())
    }

    fun loadHistoryTracks(): List<Track> {
        return searchHistoryInteractor.getHistory().map { it.toDto() }
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
    }
}

// Мапперы между DTO и Domain
fun Track.toDomain(): TrackDomainModel = TrackDomainModel(
    trackId = this.trackId,
    trackName = this.trackName,
    artistName = this.artistName,
    trackTimeMillis = this.trackTimeMillis,
    artworkUrl100 = this.artworkUrl100,
    collectionName = this.collectionName,
    releaseDate = this.releaseDate,
    primaryGenreName = this.primaryGenreName,
    country = this.country,
    previewUrl = this.previewUrl
)

fun TrackDomainModel.toDto(): Track = Track(
    trackId = this.trackId,
    trackName = this.trackName,
    artistName = this.artistName,
    trackTimeMillis = this.trackTimeMillis,
    artworkUrl100 = this.artworkUrl100,
    collectionName = this.collectionName,
    releaseDate = this.releaseDate,
    primaryGenreName = this.primaryGenreName,
    country = this.country,
    previewUrl = this.previewUrl
)