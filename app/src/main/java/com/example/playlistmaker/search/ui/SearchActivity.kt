package com.example.playlistmaker.search.ui

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
    private val clickDebounceDelay = 1000L
    private val clickHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("SearchActivity", "onCreate called")
        setContentView(R.layout.activity_search)

        setupViews()
        observeViewModel()
        android.util.Log.d("SearchActivity", "onCreate completed")
    }

    private fun setupViews() {
        android.util.Log.d("SearchActivity", "setupViews called")
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.tracksRecyclerView)
        placeholderStub = findViewById(R.id.placeholder_stub)
        errorStub = findViewById(R.id.error_stub)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        progressBar = findViewById(R.id.progressBar)
        
        android.util.Log.d("SearchActivity", "Views found - clearHistoryButton: $clearHistoryButton, historyTitle: $historyTitle, clearButton: $clearButton")
        android.util.Log.d("SearchActivity", "clearButton properties - isVisible: ${clearButton.isVisible}, width: ${clearButton.width}, height: ${clearButton.height}")

        trackAdapter = TrackAdapter { track ->
            debounceClick {
                viewModel.onTrackClick(track)
                val intent = Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                }
                startActivity(intent)
            }
            clearButton.isVisible = searchEditText.text.isNotEmpty()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        findViewById<Toolbar>(R.id.search_header).setNavigationOnClickListener { finish() }

        clearButton.setOnClickListener {
            android.util.Log.d("SearchActivity", "clearButton clicked - clearing text and hiding keyboard")
            searchEditText.text.clear()
            hideKeyboard()
        }
        android.util.Log.d("SearchActivity", "clearButton OnClickListener set")

        clearHistoryButton.setOnClickListener {
            android.util.Log.d("SearchActivity", "clearHistoryButton clicked")
            viewModel.clearHistory()
        }

        // Временная кнопка для добавления тестовой истории (для отладки)
        historyTitle.setOnLongClickListener {
            android.util.Log.d("SearchActivity", "Adding test history")
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
                android.util.Log.d("SearchActivity", "onTextChanged: before setting - query='$query', shouldShowClearButton=$shouldShowClearButton, clearButton.isVisible=${clearButton.isVisible}")
                clearButton.isVisible = shouldShowClearButton
                android.util.Log.d("SearchActivity", "onTextChanged: after setting - clearButton.isVisible=${clearButton.isVisible}")
                android.util.Log.d("SearchActivity", "onTextChanged: query='$query'")

                if (query.isEmpty()) {
                    android.util.Log.d("SearchActivity", "Query is empty, calling showHistoryIfAvailable")
                    viewModel.showHistoryIfAvailable()
                } else {
                    // Скрываем кнопку очистить историю только когда пользователь вводит текст
                    android.util.Log.d("SearchActivity", "Query is not empty, hiding history elements")
                    
                    if (isNetworkAvailable()) {
                        viewModel.searchWithDebounce(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            android.util.Log.d("SearchActivity", "onFocusChange: hasFocus=$hasFocus, text='${searchEditText.text}'")
            if (hasFocus) {
                showKeyboard()
                if (searchEditText.text.isEmpty()) {
                    android.util.Log.d("SearchActivity", "Field focused and empty, calling showHistoryIfAvailable")
                    viewModel.showHistoryIfAvailable()
                }
            }
        }

        searchEditText.requestFocus()
        android.util.Log.d("SearchActivity", "Search field focused, text: '${searchEditText.text}'")
        showKeyboard()
        android.util.Log.d("SearchActivity", "setupViews completed - clearButton.isVisible=${clearButton.isVisible}")
        android.util.Log.d("SearchActivity", "setupViews completed - clearButton final state: isVisible=${clearButton.isVisible}")
        android.util.Log.d("SearchActivity", "setupViews completed - clearButton final state: isVisible=${clearButton.isVisible}")
        android.util.Log.d("SearchActivity", "setupViews completed - clearButton final state: isVisible=${clearButton.isVisible}")
        android.util.Log.d("SearchActivity", "setupViews completed - clearButton final state: isVisible=${clearButton.isVisible}")
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            android.util.Log.d("SearchActivity", "State changed to: $state")
            when (state) {
                is SearchState.Initial -> {
                    android.util.Log.d("SearchActivity", "Initial state - no content to show")
                    hideAllPlaceholders()
                    recyclerView.isVisible = false
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = false
                    // Управляем видимостью кнопки очистки в зависимости от наличия текста
                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                    android.util.Log.d("SearchActivity", "Initial state - clearButton.isVisible=$hasText")
                }
                is SearchState.Loading -> {
                    android.util.Log.d("SearchActivity", "Loading state - hiding history elements")
                    hideAllPlaceholders()
                    recyclerView.isVisible = false
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = true

                }
                is SearchState.Content -> {
                    android.util.Log.d("SearchActivity", "Content state - hiding history elements")
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    progressBar.isVisible = false
                    trackAdapter.submitList(state.tracks)
                    recyclerView.isVisible = true

                }
                is SearchState.Empty -> {
                    android.util.Log.d("SearchActivity", "Empty state - showing empty placeholder")
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    recyclerView.isVisible = false
                    progressBar.isVisible = false
                    showEmptyPlaceholder()
                    // Управляем видимостью кнопки очистки в зависимости от наличия текста
                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                    android.util.Log.d("SearchActivity", "Empty state - clearButton.isVisible=$hasText")
                }
                is SearchState.Error -> {
                    android.util.Log.d("SearchActivity", "Error state - showing error placeholder")
                    hideAllPlaceholders()
                    historyTitle.isVisible = false
                    clearHistoryButton.isVisible = false
                    recyclerView.isVisible = false
                    progressBar.isVisible = false
                    showErrorPlaceholder()
                    // Управляем видимостью кнопки очистки в зависимости от наличия текста
                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                    android.util.Log.d("SearchActivity", "Error state - clearButton.isVisible=$hasText")
                }
                is SearchState.History -> {
                    android.util.Log.d("SearchActivity", "History state - tracks: ${state.tracks.size}")
                    hideAllPlaceholders()
                    progressBar.isVisible = false
                    trackAdapter.submitList(state.tracks)
                    recyclerView.isVisible = true
                    historyTitle.isVisible = true
                    clearHistoryButton.isVisible = true
                    // Управляем видимостью кнопки очистки в зависимости от наличия текста
                    val hasText = searchEditText.text.isNotEmpty()
                    clearButton.isVisible = hasText
                    android.util.Log.d("SearchActivity", "History state - showing ${state.tracks.size} tracks")
                    android.util.Log.d("SearchActivity", "History elements - title: visible, clearHistoryButton: visible, clearButton: $hasText")
                }
            }
        }
    }

    private fun debounceClick(action: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            action()
            clickHandler.postDelayed({ isClickAllowed = true }, clickDebounceDelay)
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