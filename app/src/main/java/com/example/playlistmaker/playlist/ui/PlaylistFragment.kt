package com.example.playlistmaker.playlist.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.player.ui.PlayerActivity
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.util.dpToPxConvert
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val args: PlaylistFragmentArgs by navArgs()
    private val viewModel: PlaylistViewModel by viewModel()
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<*>

    private val trackAdapter = TrackAdapter(
        onTrackClick = { track ->
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_TRACK, track)
            }
            startActivity(intent)
        },
        onTrackLongClick = { track ->
            showDeleteTrackDialog(track)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        viewModel.loadPlaylist(args.playlistId)
    }

    override fun onResume() {
        super.onResume()
        // Перезагружаем данные при возврате на экран (например, после редактирования)
        viewModel.loadPlaylist(args.playlistId)
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.shareButton.setOnClickListener {
            sharePlaylist()
        }

        binding.menuButton.setOnClickListener {
            showMenuBottomSheet()
        }

        setupBottomSheet()
        setupRecyclerView()
    }

    private fun setupBottomSheet() {
        // Настройка Bottom Sheet для списка треков
        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
            peekHeight = 400
            skipCollapsed = false
        }

        // Настройка Bottom Sheet для меню
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
        }

        // Настройка затемнения при открытии меню
        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                    }
                    else -> {
                        binding.overlay.isVisible = true
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = kotlin.math.abs(slideOffset)
            }
        })

        // Закрытие меню при нажатии на overlay
        binding.overlay.setOnClickListener {
            hideMenuBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = trackAdapter
    }

    private fun showDeleteTrackDialog(track: Track) {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_track_question)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.removeTrack(track.trackId)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        dialog.show()
        
        // Применяем стили к кнопкам после показа диалога
        val buttonTextColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dialog_button_text_color)
        val buttonTextSize = 14f
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(buttonTextColor)
            textSize = buttonTextSize
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical)
            )
            minHeight = resources.getDimensionPixelSize(R.dimen.dialog_button_min_height)
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(buttonTextColor)
            textSize = buttonTextSize
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical)
            )
            minHeight = resources.getDimensionPixelSize(R.dimen.dialog_button_min_height)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            state.playlist?.let { playlist ->
                bindPlaylist(playlist)
                bindTrackCount(playlist.trackCount)
            }
            bindTracks(state.tracks)
            bindDuration(state.totalDuration)
        }
    }

    private fun sharePlaylist() {
        val shareText = viewModel.getPlaylistShareText()
        if (shareText == null) {
            Toast.makeText(requireContext(), R.string.no_tracks_to_share, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
    }

    private fun showMenuBottomSheet() {
        val state = viewModel.state.value ?: return
        val playlist = state.playlist ?: return

        // Заполняем информацию о плейлисте
        binding.menuPlaylistNameTextView.text = playlist.name

        if (!playlist.description.isNullOrEmpty()) {
            binding.menuPlaylistDescriptionTextView.text = playlist.description
            binding.menuPlaylistDescriptionTextView.isVisible = true
        } else {
            binding.menuPlaylistDescriptionTextView.isVisible = false
        }

        binding.menuPlaylistInfoTextView.text = resources.getQuantityString(
            R.plurals.playlist_track_count,
            playlist.trackCount,
            playlist.trackCount
        )

        // Обработка нажатий на пункты меню
        binding.editMenuItem.setOnClickListener {
            hideMenuBottomSheet()
            navigateToEditPlaylist()
        }

        binding.shareMenuItem.setOnClickListener {
            hideMenuBottomSheet()
            sharePlaylist()
        }

        binding.deleteMenuItem.setOnClickListener {
            hideMenuBottomSheet()
            showDeletePlaylistDialog()
        }

        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun navigateToEditPlaylist() {
        val playlistId = args.playlistId
        findNavController().navigate(
            PlaylistFragmentDirections.actionPlaylistFragmentToEditPlaylistFragment(playlistId)
        )
    }

    private fun showDeletePlaylistDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_question)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deletePlaylist()
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Применяем стили к кнопкам после показа диалога
        val buttonTextColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dialog_button_text_color)
        val buttonTextSize = 14f

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(buttonTextColor)
            textSize = buttonTextSize
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical)
            )
            minHeight = resources.getDimensionPixelSize(R.dimen.dialog_button_min_height)
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(buttonTextColor)
            textSize = buttonTextSize
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.dialog_button_padding_vertical)
            )
            minHeight = resources.getDimensionPixelSize(R.dimen.dialog_button_min_height)
        }
    }

    private fun bindPlaylist(playlist: com.example.playlistmaker.playlist.domain.Playlist) {
        binding.playlistNameTextView.text = playlist.name

        if (!playlist.description.isNullOrEmpty()) {
            binding.playlistDescriptionTextView.text = playlist.description
            binding.playlistDescriptionTextView.isVisible = true
        } else {
            binding.playlistDescriptionTextView.isVisible = false
        }

        val placeholderRes = if (isInNightMode(requireContext())) {
            R.drawable.ic_cover_placeholder_night
        } else {
            R.drawable.ic_cover_placeholder
        }

        val coverFile = playlist.coverPath?.let { File(it) }
        Glide.with(requireContext())
            .load(coverFile)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .transform(RoundedCorners(dpToPxConvert.dpToPx(requireContext(), 8)))
            .into(binding.coverImageView)
    }

    private fun bindTracks(tracks: List<Track>) {
        trackAdapter.submitList(tracks)
        // Показываем Bottom Sheet только если есть треки
        binding.tracksBottomSheet.isVisible = tracks.isNotEmpty()
    }

    private fun bindDuration(totalDurationMillis: Long) {
        // Вычисляем общую длительность в минутах
        val totalMinutes = totalDurationMillis / (1000 * 60)
        
        // Используем SimpleDateFormat для форматирования (как указано в требованиях)
        val dateFormat = SimpleDateFormat("mm", Locale.getDefault())
        // Создаем Date из суммы миллисекунд для форматирования
        val date = java.util.Date(totalDurationMillis)
        val formattedMinutesStr = dateFormat.format(date)
        
        // Вычисляем часы и минуты из общего количества минут
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60

        val durationText = if (hours > 0) {
            String.format("%d ч %d мин", hours, remainingMinutes)
        } else {
            String.format("%d мин", remainingMinutes)
        }

        binding.totalDurationTextView.text = durationText
    }

    private fun bindTrackCount(trackCount: Int) {
        binding.trackCountTextView.text = resources.getQuantityString(
            R.plurals.playlist_track_count,
            trackCount,
            trackCount
        )
    }

    private fun isInNightMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }
}

