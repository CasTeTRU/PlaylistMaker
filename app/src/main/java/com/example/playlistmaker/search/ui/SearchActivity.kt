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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.PlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var trackAdapter: TrackAdapter
    private var errorLayout: View? = null
    private var emptyLayout: View? = null

    private var isClickAllowed = true
    private val clickDebounceDelay = 1000L
    private val clickHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        trackAdapter = TrackAdapter(emptyList()) { track ->
            debounceClick {
                viewModel.onTrackClick(track)
                val intent = Intent(this@SearchActivity, PlayerActivity::class.java).apply {
                    putExtra("track", track)
                }
                startActivity(intent)
            }
        }

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = trackAdapter

        binding.searchHeader.setNavigationOnClickListener { finish() }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            hideKeyboard()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
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

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.isVisible = s?.isNotEmpty() == true
                val query = s?.toString()?.trim() ?: ""

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

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
                if (binding.searchEditText.text.isEmpty()) {
                    viewModel.showHistoryIfAvailable()
                }
            }
        }

        binding.searchEditText.requestFocus()
        showKeyboard()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SearchState.Initial -> {
                    hideAllPlaceholders()
                    binding.tracksRecyclerView.isVisible = false
                    binding.historyTitle.isVisible = false
                    binding.clearHistoryButton.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is SearchState.Loading -> {
                    hideAllPlaceholders()
                    binding.tracksRecyclerView.isVisible = false
                    binding.historyTitle.isVisible = false
                    binding.clearHistoryButton.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is SearchState.Content -> {
                    hideAllPlaceholders()
                    binding.historyTitle.isVisible = false
                    binding.clearHistoryButton.isVisible = false
                    binding.progressBar.isVisible = false
                    trackAdapter.updateData(state.tracks)
                    binding.tracksRecyclerView.isVisible = true
                }
                is SearchState.Empty -> {
                    hideAllPlaceholders()
                    binding.historyTitle.isVisible = false
                    binding.clearHistoryButton.isVisible = false
                    binding.tracksRecyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    showEmptyPlaceholder()
                }
                is SearchState.Error -> {
                    hideAllPlaceholders()
                    binding.historyTitle.isVisible = false
                    binding.clearHistoryButton.isVisible = false
                    binding.tracksRecyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    showErrorPlaceholder()
                }
                is SearchState.History -> {
                    hideAllPlaceholders()
                    binding.progressBar.isVisible = false
                    if (binding.searchEditText.text.isEmpty() && binding.searchEditText.hasFocus()) {
                        trackAdapter.updateData(state.tracks)
                        binding.tracksRecyclerView.isVisible = true
                        binding.historyTitle.isVisible = true
                        binding.clearHistoryButton.isVisible = true
                    }
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
            emptyLayout = binding.placeholderStub.inflate()
        }
        emptyLayout?.isVisible = true
    }

    private fun showErrorPlaceholder() {
        if (errorLayout == null) {
            errorLayout = binding.errorStub.inflate()
            val retryBtn = errorLayout?.findViewById<Button>(R.id.retryButton)
            retryBtn?.setOnClickListener {
                val query = binding.searchEditText.text.toString().trim()
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
        binding.searchEditText.requestFocus()
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (binding.searchEditText.isFocused) {
            imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }
    }
}