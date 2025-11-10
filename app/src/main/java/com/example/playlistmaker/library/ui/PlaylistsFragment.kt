package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PlaylistsViewModel by viewModel()

    private val playlistAdapter = PlaylistAdapter { playlist ->
        // Используем навигацию через родительский фрагмент
        requireParentFragment().findNavController().navigate(
            R.id.action_libraryFragment_to_playlistFragment,
            android.os.Bundle().apply {
                putLong("playlistId", playlist.id)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.newPlaylistBtn.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
        }

        binding.playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.adapter = playlistAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.playlistsFlow.collect { playlists ->
                    if (playlists.isEmpty()) {
                        showEmpty()
                    } else {
                        showContent(playlists)
                    }
                }
            }
        }
    }

    private fun showContent(playlists: List<com.example.playlistmaker.playlist.domain.Playlist>) {
        binding.playlistsRecyclerView.isVisible = true
        binding.emptyPlaceholder.isVisible = false
        playlistAdapter.submitList(playlists)
    }

    private fun showEmpty() {
        binding.playlistsRecyclerView.isVisible = false
        binding.emptyPlaceholder.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}

