package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var originalTracks: List<Track>

    private var initialTracksLoaded = false


    data class Track(
        val trackName: String,
        val artistName: String,
        val trackTime: String,
        val artworkUrl100: String
    )


    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artwork: ImageView = itemView.findViewById(R.id.artworkImageView)
        private val trackName: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val artistName: TextView = itemView.findViewById(R.id.artistNameTextView)
        private val trackTime: TextView = itemView.findViewById(R.id.trackTimeTextView)

        fun bind(track: Track) {
            trackName.text = track.trackName
            artistName.text = "${track.artistName} • ${track.trackTime}"

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .centerCrop()
                .transform(RoundedCorners(12))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(artwork)
        }
    }


    class TrackAdapter(tracks: List<Track>) : RecyclerView.Adapter<TrackViewHolder>() {

        private var trackList = tracks

        fun updateData(newTracks: List<Track>) {
            trackList = newTracks
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track, parent, false)
            return TrackViewHolder(view)
        }

        override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
            holder.bind(trackList[position])
        }

        override fun getItemCount() = trackList.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.tracksRecyclerView)

        val backButton = findViewById<Toolbar>(R.id.search_header)


        originalTracks = getSampleTracks() // Сохраняем оригинальные данные
        trackAdapter = TrackAdapter(emptyList()) // Изначально пустой список
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        backButton.setNavigationOnClickListener { finish() }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
        }

        fun updateRecyclerViewVisibility(adapter: TrackAdapter, filteredList: List<Track>) {
            adapter.updateData(filteredList)

            val layoutParams = recyclerView.layoutParams
            if (filteredList.isEmpty()) {
                layoutParams.height = 0
                recyclerView.visibility = View.GONE
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                recyclerView.visibility = View.VISIBLE
            }

            recyclerView.layoutParams = layoutParams
            recyclerView.requestLayout()
        }

        fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            clearButton.isVisible = s?.isNotEmpty() == true

            val query = s.toString().trim().lowercase()

            val filteredList = if (query.isEmpty()) {
                originalTracks
            } else {
                originalTracks.filter { track ->
                    track.trackName.lowercase().contains(query) ||
                            track.artistName.lowercase().contains(query)
                }
            }

            println("FilteredList size: ${filteredList.size}")
            updateRecyclerViewVisibility(trackAdapter, filteredList)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.isVisible = s?.isNotEmpty() == true

                val query = s.toString().trim().lowercase()

                val filteredList = if (query.isEmpty()) {
                    emptyList() // Не показываем весь список по умолчанию
                } else {
                    originalTracks.filter { track ->
                        track.trackName.lowercase().contains(query) ||
                                track.artistName.lowercase().contains(query)
                    }
                }

                updateRecyclerViewVisibility(trackAdapter, filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showKeyboard()
        }
    }

    private fun getSampleTracks(): List<Track> {
        return listOf(
            Track(
                "Smells Like Teen Spirit",
                "Nirvana",
                "5:01",
                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Billie Jean",
                "Michael Jackson",
                "4:35",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                "Stayin' Alive",
                "Bee Gees",
                "4:10",
                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                "Whole Lotta Love",
                "Led Zeppelin",
                "5:33",
                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                "Sweet Child O'Mine",
                "Guns N' Roses",
                "5:03",
                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )
        )
    }


    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}