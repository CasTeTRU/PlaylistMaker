package com.example.playlistmaker.search.ui

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.player.ui.PlayerFragment
import com.google.android.material.internal.ViewUtils.hideKeyboard
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private val trackAdapter = TrackAdapter { track ->
        val args = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(R.id.playerFragment, args)
    }

    private var textWatcher: android.text.TextWatcher? = null
    private var inputValue: String = INPUT_DEF

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 100L
        private const val INPUT_STATE = "INPUT_STATE"
        private const val INPUT_DEF = ""
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


        savedInstanceState?.getString(INPUT_STATE)?.let {
            inputValue = it
        }

        try {
            setupViews()
            observeState()

        } catch (e: Exception) {
            Log.e("SearchFragment", "Error in onViewCreated", e)
            showErrorState()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_STATE, inputValue)
    }

    private fun setupViews() {
        try {

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = trackAdapter

            binding.clearButton.setOnClickListener {
                binding.searchView.text.clear()

            }

            binding.clearHistoryButton.setOnClickListener {
                viewModel.clearHistory()
                binding.historyTitle.isVisible = false
                binding.clearHistoryButton.isVisible = false
            }

            binding.searchView.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim() ?: ""
                    val shouldShowClearButton = s?.isNotEmpty() == true
                    binding.clearButton.isVisible = shouldShowClearButton

                    binding.historyTitle.isVisible = false

                    if (query.isNotEmpty()) {
                        viewModel.searchWithDebounce(query)
                        binding.recyclerView.isVisible = true
                    } else {
                        viewModel.showHistoryIfAvailable()
                        binding.recyclerView.isVisible = false
                    }

                    inputValue = query
                }

                override fun afterTextChanged(s: android.text.Editable?) {
                    binding.placeholderEmptySearch.root.isVisible = false
                    binding.placeholderServerError.root.isVisible = false
                }
            })

            binding.searchView.setOnFocusChangeListener { _, hasFocus ->
                val query = binding.searchView.text.toString()
                binding.historyTitle.isVisible = false
            }

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
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false

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

        trackAdapter.submitList(tracks)
        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showHistoryState(tracks: List<Track>) {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = true
        binding.clearHistoryButton.isVisible = true
        binding.placeholderEmptySearch.root.isVisible = false
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false

        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }

    private fun showEmptyState() {
        binding.recyclerView.isVisible = false
        binding.historyTitle.isVisible = false
        binding.clearHistoryButton.isVisible = false
        binding.placeholderEmptySearch.root.isVisible = true
        binding.placeholderServerError.root.isVisible = false
        binding.progressBar.isVisible = false

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

        val hasText = binding.searchView.text.isNotEmpty()
        binding.clearButton.isVisible = hasText
    }


    private fun historyActions(track: Track) {
        fun removeAt(position: Int) {
            // no-op
        }

        fun showToast(text: String?) {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }

        fun hideKeyboard() {
            val inputMethodManager =
                requireActivity().getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
        }

        fun onResume() {
            super.onResume()
        }

        fun onDestroyView() {
            super.onDestroyView()
            textWatcher?.let { binding.searchView.removeTextChangedListener(it) }
            _binding = null
        }
    }
}