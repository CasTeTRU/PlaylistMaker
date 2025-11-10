package com.example.playlistmaker.search.ui

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.PlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModel()

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
    private lateinit var progressBar: ProgressBar

    private var isClickAllowed = true
    private val CLICK_DEBOUNCE_DELAY = 1000L
    private var clickDebounceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.tracksRecyclerView)
        placeholderStub = findViewById(R.id.placeholder_stub)
        errorStub = findViewById(R.id.error_stub)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        progressBar = findViewById(R.id.progressBar)

        trackAdapter = TrackAdapter(
            onTrackClick = { track ->
                debounceClick {
                    viewModel.onTrackClick(track)
                    val intent = Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                        putExtra("track", track)
                    }
                    startActivity(intent)
                }
                clearButton.isVisible = searchEditText.text.isNotEmpty()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        findViewById<Toolbar>(R.id.search_header).setNavigationOnClickListener { finish() }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }


        historyTitle.setOnLongClickListener {
            viewModel.addTestHistory()
            true
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    if (isNetworkAvailable()) {
                        viewModel.searchTracks(query)
                    } else {
                        showErrorPlaceholder()
                    }
                }
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                val shouldShowClearButton = s?.isNotEmpty() == true
                clearButton.isVisible = shouldShowClearButton

                if (query.isEmpty()) {
                    viewModel.showHistoryIfAvailable()
                } else {

                    if (isNetworkAvailable()) {
                        viewModel.searchWithDebounce(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
                if (searchEditText.text.isEmpty()) {
                    viewModel.showHistoryIfAvailable()
                }
            }
        }

        searchEditText.requestFocus()
        showKeyboard()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SearchState.Initial -> {
                    hideAllPlaceholders()
                    recyclerView.isVisible = false
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = false
                    // Управляем видимостью кнопки очистки в зависимости от наличия текста
                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                }
                is SearchState.Loading -> {
                    hideAllPlaceholders()
                    recyclerView.isVisible = false
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = true

                }
                is SearchState.Content -> {
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = false
                    trackAdapter.submitList(state.tracks)
                    recyclerView.isVisible = true

                }
                is SearchState.Empty -> {
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    recyclerView.isVisible = false
                    progressBar.isVisible = false
                    showEmptyPlaceholder()

                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                }
                is SearchState.Error -> {
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    recyclerView.isVisible = false
                    progressBar.isVisible = false
                    showErrorPlaceholder()

                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                }
                is SearchState.History -> {
                    hideAllPlaceholders()
                    progressBar.isVisible = false
                    trackAdapter.submitList(state.tracks)
                    recyclerView.isVisible = true
                    historyTitle.isVisible = true
                    clearHistoryButton.isVisible = true

                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                }
            }
        }
    }

    private fun debounceClick(action: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            action()
            clickDebounceJob?.cancel()
            clickDebounceJob = lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun showEmptyPlaceholder() {
        if (emptyLayout == null) {
            emptyLayout = placeholderStub.inflate()
        }
        emptyLayout?.isVisible = true
    }

    private fun showErrorPlaceholder() {
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
                    viewModel.searchTracks(query)
                }
            }
        }
        errorLayout?.isVisible = true
    }

    private fun hideAllPlaceholders() {
        emptyLayout?.isVisible = false
        errorLayout?.isVisible = false
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
}