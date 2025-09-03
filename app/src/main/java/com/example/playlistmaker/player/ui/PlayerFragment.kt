package com.example.playlistmaker.player.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.search.domain.Track

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()

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
        
        // Получаем трек из аргументов
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }
        
        track?.let {
            // Создаем ViewModel с треком
            // TODO: Исправить создание ViewModel
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
            // TODO: Реализовать предыдущий трек
        }

        binding.btnNext.setOnClickListener {
            // TODO: Реализовать следующий трек
        }

        binding.btnLike.setOnClickListener {
            // TODO: Реализовать лайк
        }

        binding.btnShare.setOnClickListener {
            // TODO: Реализовать шаринг
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
                
                // TODO: Добавить логику для лайка
            }
        }
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
    }
}
