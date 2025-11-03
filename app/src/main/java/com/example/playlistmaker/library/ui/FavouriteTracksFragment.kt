package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue
class FavouriteTracksFragment : Fragment() {

    private val viewModel by viewModel<FavoriteViewModel>()
    private var _binding: FragmentFavouriteTracksBinding? = null
    private val binding get() = _binding!!

    private lateinit var onTrackClickDebounce: (Track) -> Unit

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
        // В текущем макете нет списка избранного. Просто наблюдаем состояние на случай будущего расширения.
        viewModel.observeFavorite().observe(viewLifecycleOwner) { /* no-op for now */ }
    }

    private fun showContent(tracks: List<Track>) {
        // Макет не содержит списка — пропускаем вывод
    }

    private fun showEmpty(){
        // Макет показывает статические плейсхолдеры — ничего не делаем
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

