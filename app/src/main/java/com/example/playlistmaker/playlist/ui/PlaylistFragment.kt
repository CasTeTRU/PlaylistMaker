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
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.player.ui.PlayerActivity
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.domain.Track
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
    private lateinit var menuBottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    private val trackAdapter = TrackAdapter(
        onTrackClick = { track ->
            try {
                android.util.Log.d("PlaylistFragment", "Track clicked: trackId=${track.trackId}, trackName=${track.trackName}")
                
                // Проверяем, что трек валиден перед передачей
                if (track.trackId.isBlank() || track.trackName.isBlank() || track.artistName.isBlank()) {
                    android.util.Log.e("PlaylistFragment", "Invalid track data: trackId=${track.trackId}, trackName=${track.trackName}, artistName=${track.artistName}")
                    Toast.makeText(requireContext(), "Ошибка: некорректные данные трека", Toast.LENGTH_SHORT).show()
                    return@TrackAdapter
                }
                
                android.util.Log.d("PlaylistFragment", "Creating intent with track")
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_TRACK, track)
                }
                android.util.Log.d("PlaylistFragment", "Starting PlayerActivity")
                startActivity(intent)
                android.util.Log.d("PlaylistFragment", "PlayerActivity started successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlaylistFragment", "Error starting PlayerActivity", e)
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка при запуске плеера: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false // Нельзя скрыть
            peekHeight = (266 * resources.displayMetrics.density).toInt()
            skipCollapsed = false // Позволяет переходить в STATE_COLLAPSED
            // Добавляем callback для предотвращения скрытия
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // Предотвращаем переход в STATE_HIDDEN
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Ничего не делаем при скольжении
                }
            })
        }
        
        // Вычисляем peekHeight на основе реальной позиции кнопок после отрисовки
        binding.shareButton.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Удаляем listener после первого вызова
                binding.shareButton.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Вычисляем peekHeight
                updatePeekHeight()
            }
        })

        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
        }

        menuBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
                _binding?.overlay?.isVisible = isVisible
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.overlay?.alpha = kotlin.math.abs(slideOffset)
            }
        }

        menuBottomSheetBehavior.addBottomSheetCallback(menuBottomSheetCallback)

        binding.overlay.setOnClickListener {
            hideMenuBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = trackAdapter
    }
    
    private fun updatePeekHeight() {
        // Используем post для расчета после полной отрисовки
        binding.shareButton.post {
            val buttonsContainer = binding.shareButton.parent as? ViewGroup
            buttonsContainer?.let { container ->
                val coordinatorLayout = binding.root
                
                // Получаем позицию контейнера с кнопками и CoordinatorLayout в окне
                val containerLocation = IntArray(2)
                container.getLocationInWindow(containerLocation)
                val coordinatorLocation = IntArray(2)
                coordinatorLayout.getLocationInWindow(coordinatorLocation)
                
                // Вычисляем нижнюю границу кнопок относительно CoordinatorLayout
                val buttonsBottomY = containerLocation[1] - coordinatorLocation[1] + container.height
                val coordinatorHeight = coordinatorLayout.height
                val offset24dp = (24 * resources.displayMetrics.density).toInt()
                
                // peekHeight - это высота bottom sheet от низа CoordinatorLayout
                // Верхняя граница bottom sheet должна быть на buttonsBottomY + 24dp от верха CoordinatorLayout
                // Значит peekHeight = coordinatorHeight - (buttonsBottomY + 24dp)
                val targetTopY = buttonsBottomY + offset24dp
                val peekHeight = coordinatorHeight - targetTopY
                
                // Обновляем peekHeight на вычисленное значение
                // Убеждаемся, что peekHeight положительный и разумный
                val minPeekHeight = (200 * resources.displayMetrics.density).toInt()
                if (peekHeight > minPeekHeight) {
                    tracksBottomSheetBehavior.peekHeight = peekHeight
                } else {
                    // Если расчет дал слишком маленькое значение, используем минимальное
                    tracksBottomSheetBehavior.peekHeight = minPeekHeight
                }
            }
        }
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

        // Настройка для темной темы
        if (isInNightMode(requireContext())) {
            // Фон диалога - YP White
            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.yp_white)
                )
            )
            
            // Текст сообщения - YP Black
            val messageView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
            messageView?.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.yp_black)
            )
        }

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

        viewModel.isPlaylistDeleted.observe(viewLifecycleOwner) { isDeleted ->
            if (isDeleted) {
                findNavController().popBackStack()
            }
        }
    }

    private fun sharePlaylist() {
        val shareText = viewModel.getPlaylistShareText(resources)
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

        _binding?.let { binding ->
            binding.menuPlaylistNameTextView.text = playlist.name

            // Загружаем обложку плейлиста в меню
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
                .into(binding.menuCoverImageView)

            binding.menuPlaylistInfoTextView.text = resources.getQuantityString(
                R.plurals.playlist_track_count,
                playlist.trackCount,
                playlist.trackCount
            )

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
        }

        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun navigateToEditPlaylist() {
        val state = viewModel.state.value ?: return
        val playlist = state.playlist ?: return
        findNavController().navigate(
            PlaylistFragmentDirections.actionPlaylistFragmentToCreatePlaylistFragment(playlist)
        )
    }

    private fun showDeletePlaylistDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_question)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deletePlaylist()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Настройка для темной темы
        if (isInNightMode(requireContext())) {
            // Фон диалога - YP White
            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.yp_white)
                )
            )
            
            // Текст заголовка - YP Black
            val titleView = dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.yp_black)
            )
            
            // Текст сообщения - YP Black
            val messageView = dialog.findViewById<android.widget.TextView>(android.R.id.message)
            messageView?.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.yp_black)
            )
        }

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
        _binding?.let { binding ->
            binding.playlistNameTextView.text = playlist.name

            if (!playlist.description.isNullOrEmpty()) {
                binding.playlistDescriptionTextView.text = playlist.description
                binding.playlistDescriptionTextView.isVisible = true
            } else {
                binding.playlistDescriptionTextView.isVisible = false
            }
            
            // Пересчитываем peekHeight после изменения видимости описания
            // Используем двойной post для гарантии, что layout пересчитался
            binding.playlistDescriptionTextView.post {
                binding.playlistDescriptionTextView.post {
                    updatePeekHeight()
                }
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
                .into(binding.coverImageView)
        }
    }

    private fun bindTracks(tracks: List<Track>) {
        trackAdapter.submitList(tracks)
        _binding?.let { binding ->
            binding.tracksBottomSheet.isVisible = tracks.isNotEmpty()
            binding.emptyTracksTextView.isVisible = tracks.isEmpty()
        }
    }

    private fun bindDuration(totalDurationMillis: Long) {
        val totalMinutes = totalDurationMillis / (1000 * 60)
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60

        val durationText = if (hours > 0) {
            String.format("%d ч %d мин", hours, remainingMinutes)
        } else {
            String.format("%d мин", remainingMinutes)
        }

        _binding?.totalDurationTextView?.text = durationText
    }

    private fun bindTrackCount(trackCount: Int) {
        _binding?.trackCountTextView?.text = resources.getQuantityString(
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
        menuBottomSheetBehavior.removeBottomSheetCallback(menuBottomSheetCallback)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }
}