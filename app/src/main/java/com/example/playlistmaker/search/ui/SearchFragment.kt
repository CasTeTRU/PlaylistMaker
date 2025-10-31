package com.example.playlistmaker.search.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.domain.Track
import org.koin.android.ext.android.inject

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SearchViewModel by inject()
    private val adapter = TrackAdapter { track -> 
        android.util.Log.d("SearchFragment", "Track clicked: ${track.trackName} by ${track.artistName}")
        viewModel.onTrackClick(track)

        val intent = android.content.Intent(requireContext(), com.example.playlistmaker.player.ui.PlayerActivity::class.java).apply {
            putExtra("track", track)
        }
        android.util.Log.d("SearchFragment", "Starting PlayerActivity with track: ${track.trackId}")
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d("SearchFragment", "onViewCreated started")
            
            setupViews()
            observeState()
            
            Log.d("SearchFragment", "onViewCreated completed successfully")
        } catch (e: Exception) {
            Log.e("SearchFragment", "Error in onViewCreated", e)
            showErrorState()
        }
    }

    private fun setupViews() {
        try {
            // Настройка RecyclerView
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
            
            // Настройка кнопки очистки
            binding.clearButton.setOnClickListener {
                android.util.Log.d("SearchFragment", "clearButton clicked")
                binding.searchView.text.clear()
            }
            
            // Настройка кнопки очистить историю
            binding.clearHistoryButton.setOnClickListener {
                android.util.Log.d("SearchFragment", "clearHistoryButton clicked")
                viewModel.clearHistory()
            }
            
            // Настройка поиска
            binding.searchView.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim() ?: ""
                    val shouldShowClearButton = s?.isNotEmpty() == true
                    binding.clearButton.isVisible = shouldShowClearButton
                    
                    if (query.isNotEmpty()) {
                        viewModel.searchWithDebounce(query)
                    } else {
                        viewModel.showHistoryIfAvailable()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
            
            binding.searchView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchView.text.toString().trim()
                    if (query.isNotEmpty()) {
                        viewModel.searchTracks(query)
                    }
                    true
                } else {
                    false
                }
            }
            
        } catch (e: Exception) {
            Log.e("SearchFragment", "Error setting up views", e)
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Initial -> showInitialState()
                is SearchState.Loading -> showLoadingState()
                is SearchState.Content -> showContentState(state.tracks)
                is SearchState.Empty -> showEmptyState()
                is SearchState.Error -> showErrorState()
                is SearchState.History -> showHistoryState(state.tracks)
            }
        }
    }

    private fun showInitialState() {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = true
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showLoadingState() {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = true
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showContentState(tracks: List<Track>) {
        binding.recyclerView.isVisible = true
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false
        adapter.submitList(tracks)
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showHistoryState(tracks: List<Track>) {
        binding.recyclerView.isVisible = true
        binding.historyTitle.isVisible = true
        binding.clearHistoryButton.isVisible = true
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false
        adapter.submitList(tracks)
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
        android.util.Log.d("SearchFragment", "History state - showing ${tracks.size} tracks")
    }

    private fun showEmptyState() {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = true
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showErrorState() {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = true
        binding.progressBar.isVisible = false
        // Управляем видимостью кнопки очистки в зависимости от наличия текста
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
