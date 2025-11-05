package com.example.playlistmaker.player.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.search.domain.Track
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!


    private val viewModel: PlayerViewModel by viewModel { parametersOf(getTrackFromArguments()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        getTrackFromArguments()?.let { bind(it) }
    }

    private fun getTrackFromArguments(): Track? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }
    }

    private fun setupViews() {

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPlayPause.setOnClickListener {
            viewModel.playbackControl()
        }

        binding.btnPrevious.setOnClickListener {

        }

        binding.btnNext.setOnClickListener {

        }

        binding.btnLike.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        binding.btnShare.setOnClickListener {

        }
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.apply {
                tvTrackName.text = state.track.trackName
                tvArtistName.text = state.track.artistName
                tvAlbumName.text = state.track.collectionName ?: "Неизвестный альбом"

                btnPlayPause.setImageResource(
                    if (state.isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
                
                btnLike.setImageResource(
                    if (state.track.isFavorite) R.drawable.ic_favourite_checked 
                    else R.drawable.ic_like_off
                )
            }
        }

    }


    private fun bind(item: Track) {
        SimpleDateFormat("mm:ss", Locale.getDefault())
        // Загрузка обложки с Glide
        item.getCoverArtwork()?.let { coverUrl ->
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAlbumCover)
        }

        binding.apply {
            tvTrackName.text = item.trackName
            tvArtistName.text = item.artistName

            tvAlbumName.text = item.collectionName ?: "Неизвестный альбом"
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            btnLike.setImageResource(if (item.isFavorite) R.drawable.ic_favourite_checked else R.drawable.ic_like_off)
        }
    }


    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(track: Track): PlayerFragment {
            return PlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("track", track)
                }
            }
        }


        private const val ARGS_TRACK = "chosen_Track_Key"

    }
}