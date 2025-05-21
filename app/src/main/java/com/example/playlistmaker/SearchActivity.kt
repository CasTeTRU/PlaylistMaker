package com.example.playlistmaker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var placeholderStub: ViewStub
    private lateinit var errorStub: ViewStub
    private var errorLayout: View? = null
    private var emptyLayout: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.tracksRecyclerView)
        placeholderStub = findViewById(R.id.placeholder_stub)
        errorStub = findViewById(R.id.error_stub)


        trackAdapter = TrackAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        findViewById<Toolbar>(R.id.search_header).setNavigationOnClickListener { finish() }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
            trackAdapter.updateData(emptyList())
            hideAllPlaceholders()
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
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    trackAdapter.updateData(emptyList())
                    hideAllPlaceholders()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showKeyboard()
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
        if (!isNetworkAvailable()) {
            showErrorPlaceholder()
            return
        }

        hideAllPlaceholders()

        val call = NetworkClient.itunesApi.search(query)

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful && (response.body()?.resultCount ?: 0) > 0) {
                    val tracks = response.body()?.results ?: emptyList()
                    trackAdapter.updateData(tracks)
                    recyclerView.visibility = View.VISIBLE
                } else if (!response.isSuccessful) {
                    showErrorPlaceholder()
                } else {
                    showEmptyPlaceholder()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showErrorPlaceholder()
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
                    Toast.makeText(this, getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT).show()
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
        recyclerView.visibility = View.GONE
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
}