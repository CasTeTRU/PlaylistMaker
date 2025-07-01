package com.example.playlistmaker

import TrackAdapter
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        getSharedPreferences("search_history", Context.MODE_PRIVATE)
    }
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var progressBar: ProgressBar
    private var isClickAllowed = true
    private val clickDebounceDelay = 1000L // 1 секунда
    private val clickHandler = Handler(Looper.getMainLooper())

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
                        recyclerView.visibility = View.GONE
                        hideAllPlaceholders()
                        progressBar.postDelayed({
                            searchTracks(query)
                        }, 300)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 2000)
                } else {
                    trackAdapter.updateData(emptyList())
                    progressBar.visibility = View.GONE
                    showHistoryIfAvailable()
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
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun searchTracks(query: String) {
        hideAllPlaceholders()
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        val call = NetworkClient.itunesApi.search(query)

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                progressBar.visibility = View.GONE
                val hasResults = response.isSuccessful && (response.body()?.resultCount ?: 0) > 0
                if (hasResults) {
                    val tracks = response.body()?.results ?: emptyList()

                    trackAdapter.updateData(tracks)
                    recyclerView.visibility = View.VISIBLE
                } else if (!response.isSuccessful) {
                    Log.e("SearchActivity", "Error response: ${response.code()}")
                    showErrorPlaceholder()
                } else {
                    showEmptyPlaceholder()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("SearchActivity", "Network failure", t)
                if (!isNetworkAvailable()) {
                    showErrorPlaceholder()
                } else {
                    showErrorPlaceholder()
                }
            }
        })
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        searchEditText.requestFocus()
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (searchEditText.isFocused) {
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    fun saveTrackToHistory(track: Track) {
        val history = loadHistoryTracks().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            saveHistory(history.take(MAX_HISTORY_SIZE))
        } else {
            saveHistory(history)
        }
    }

    private fun saveHistory(history: List<Track>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(history)
        editor.putString(HISTORY_KEY, json)
        editor.apply()
    }

    fun loadHistoryTracks(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        val typeToken = object : TypeToken<List<Track>>() {}.type
        return Gson().fromJson(json, typeToken) ?: emptyList()
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(HISTORY_KEY).apply()
    }
}