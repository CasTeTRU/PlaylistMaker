package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.player.ui.PlayerActivity
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue
class FavouriteTracksFragment : Fragment() {

    private val viewModel by viewModel<FavoriteViewModel>()
    private var _binding: FragmentFavouriteTracksBinding? = null
    private val binding get() = _binding!!

    private val trackAdapter = TrackAdapter { track ->
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_TRACK, track)
        }
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTrack.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrack.adapter = trackAdapter

        viewModel.observeFavorite().observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun showContent(tracks: List<Track>) {
        binding.placeholderContainer.isVisible = false
        binding.rvTrack.isVisible = true
        trackAdapter.submitList(tracks)
    }

    private fun showEmpty(){
        binding.rvTrack.isVisible = false
        binding.placeholderContainer.isVisible = true
    }

    private fun render(state: FavoriteStates) {
        when (state) {
            is FavoriteStates.Empty -> showEmpty()
            is FavoriteStates.Content -> showContent(state.tracks)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.interactor()
    }

    companion object {
        fun newInstance(): FavouriteTracksFragment {
            return FavouriteTracksFragment()
        }
    }
}

