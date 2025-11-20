package com.example.playlistmaker.playlist.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

open class CreatePlaylistFragment : Fragment() {

    protected var _binding: FragmentCreatePlaylistBinding? = null
    protected val binding get() = _binding!!

    protected open val viewModel: CreatePlaylistViewModel by viewModel()
    
    // Получаем nullable плейлист из аргументов навигации
    private val args: CreatePlaylistFragmentArgs by navArgs()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        viewModel.updateCoverUri(uri)
        copyImageToPrivateStorage(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Инициализируем ViewModel переданным плейлистом (может быть null)
            viewModel.initializePlaylist(args.playlist)
            
            setupViews()
            observeViewModel()
            // Восстанавливаем состояние при первом создании view
            restoreState()
        } catch (e: Exception) {
            android.util.Log.e("CreatePlaylistFragment", "Error in onViewCreated", e)
            e.printStackTrace()
            // Пытаемся вернуться назад при ошибке
            try {
                navigateBack()
            } catch (e2: Exception) {
                android.util.Log.e("CreatePlaylistFragment", "Error navigating back", e2)
            }
        }
    }

    protected open fun restoreState() {
        // Восстанавливаем состояние из ViewModel при создании view
        // Это гарантирует, что при возврате из фонового режима данные будут восстановлены
        val currentState = viewModel.state.value ?: return
        
        // Устанавливаем текст, избегая триггера TextWatcher для предотвращения зацикливания
        if (binding.nameEditText.text.toString() != currentState.name) {
            val nameWatcher = binding.nameEditText.tag as? TextWatcher
            nameWatcher?.let { binding.nameEditText.removeTextChangedListener(it) }
            binding.nameEditText.setText(currentState.name)
            nameWatcher?.let { binding.nameEditText.addTextChangedListener(it) }
        }
        
        if (binding.descriptionEditText.text.toString() != (currentState.description ?: "")) {
            val descriptionWatcher = binding.descriptionEditText.tag as? TextWatcher
            descriptionWatcher?.let { binding.descriptionEditText.removeTextChangedListener(it) }
            binding.descriptionEditText.setText(currentState.description ?: "")
            descriptionWatcher?.let { binding.descriptionEditText.addTextChangedListener(it) }
        }
    }

    protected open fun setupViews() {
        binding.headerContainer.setOnClickListener {
            handleBackPress()
        }

        binding.coverContainer.setOnClickListener {
            pickImageLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Устанавливаем цвет курсора программно (TextInputLayout может переопределять атрибут из layout)
        binding.nameEditText.post {
            // Устанавливаем drawable курсора напрямую
            val cursorDrawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.cursor_color)
            if (cursorDrawable != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                binding.nameEditText.setTextCursorDrawable(cursorDrawable)
            }
        }
        
        binding.descriptionEditText.post {
            // Устанавливаем drawable курсора напрямую
            val cursorDrawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.cursor_color)
            if (cursorDrawable != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                binding.descriptionEditText.setTextCursorDrawable(cursorDrawable)
            }
        }
        
        // Также устанавливаем при получении фокуса
        binding.nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.nameEditText.post {
                    val cursorDrawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.cursor_color)
                    if (cursorDrawable != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        binding.nameEditText.setTextCursorDrawable(cursorDrawable)
                    }
                }
            }
        }
        
        binding.descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.descriptionEditText.post {
                    val cursorDrawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.cursor_color)
                    if (cursorDrawable != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        binding.descriptionEditText.setTextCursorDrawable(cursorDrawable)
                    }
                }
            }
        }

        // Создаём TextWatcher для поля имени, избегая зацикливания при восстановлении состояния
        val nameWatcher = binding.nameEditText.doAfterTextChanged { s: Editable? ->
            val newText = s?.toString() ?: ""
            if (viewModel.state.value?.name != newText) {
                viewModel.updateName(newText)
            }
        }
        binding.nameEditText.tag = nameWatcher // Сохраняем ссылку для возможного удаления

        // Создаём TextWatcher для поля описания, избегая зацикливания при восстановлении состояния
        val descriptionWatcher = binding.descriptionEditText.doAfterTextChanged { s: Editable? ->
            val newText = s?.toString() ?: ""
            if (viewModel.state.value?.description != newText) {
                viewModel.updateDescription(newText)
            }
        }
        binding.descriptionEditText.tag = descriptionWatcher // Сохраняем ссылку для возможного удаления

        binding.createButton.setOnClickListener {
            val state = viewModel.state.value ?: return@setOnClickListener
            viewModel.createPlaylist(state.coverPath)
        }

        // Обрабатываем системную кнопку Back и навигационную кнопку "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        )
    }

    protected open fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            // Проверяем, редактируем ли мы существующий плейлист
            val isEditMode = args.playlist != null
            
            // Устанавливаем заголовок и текст кнопки в зависимости от режима
            if (isEditMode) {
                binding.titleTextView.text = getString(R.string.edit)
                binding.createButton.text = getString(R.string.save)
            } else {
                binding.titleTextView.text = getString(R.string.new_playlist)
                binding.createButton.text = getString(R.string.create)
            }

            // Восстанавливаем состояние полей при изменении состояния
            if (binding.nameEditText.text.toString() != state.name) {
                binding.nameEditText.setText(state.name)
            }
            if (binding.descriptionEditText.text.toString() != (state.description ?: "")) {
                binding.descriptionEditText.setText(state.description ?: "")
            }
            
            binding.createButton.isEnabled = state.isCreateButtonEnabled

            if (state.coverUri != null) {
                Glide.with(this)
                    .load(state.coverUri)
                    .into(binding.coverImageView)
                binding.coverPlaceholder.isVisible = false
            } else {
                binding.coverImageView.setImageDrawable(null)
                binding.coverPlaceholder.isVisible = true
            }
        }

        viewModel.isPlaylistCreated.observe(viewLifecycleOwner) { playlistName ->
            playlistName?.let {
                showSnackbar(getString(R.string.playlist_created, it))
                if (isAdded && !isRemoving) {
                    navigateBack()
                }
            }
        }
        
        viewModel.isPlaylistUpdated.observe(viewLifecycleOwner) { isUpdated ->
            if (isUpdated) {
                if (isAdded && !isRemoving) {
                    navigateBack()
                }
            }
        }
    }

    protected open fun handleBackPress() {
        // Если редактируем плейлист, выходим без подтверждения
        if (args.playlist != null) {
            navigateBack()
        } else {
            // Если создаем новый плейлист, показываем диалог подтверждения при наличии изменений
            if (viewModel.hasUnsavedChanges()) {
                showConfirmDialog()
            } else {
                navigateBack()
            }
        }
    }

    protected open fun navigateBack() {
        if (!isAdded || isRemoving) {
            return // Фрагмент уже удаляется или не добавлен
        }
        
        try {
            val navController = findNavController()
            val playerContainer = activity?.findViewById<View>(R.id.nav_host_fragment_player)
            
            // Проверяем, находимся ли мы в PlayerActivity (есть контейнер nav_host_fragment_player)
            if (playerContainer != null) {
                // Мы в PlayerActivity - используем Navigation Component для возврата
                val canPop = navController.previousBackStackEntry != null && navController.popBackStack()
                // Если не удалось вернуться (стек пуст) или мы на startDestination, скрываем контейнер
                if (!canPop || navController.previousBackStackEntry == null) {
                    playerContainer.isVisible = false
                }
            } else {
                // Мы в MainActivity, используем стандартную навигацию через Navigation Component
                navController.popBackStack()
            }
        } catch (e: Exception) {
            android.util.Log.e("CreatePlaylistFragment", "Error navigating back via Navigation", e)
            // В случае ошибки пытаемся скрыть контейнер напрямую (если мы в PlayerActivity)
            activity?.findViewById<View>(R.id.nav_host_fragment_player)?.isVisible = false
        }
    }

    protected open fun showConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_playlist_creation)
            .setMessage(R.string.unsaved_data_warning)
            .setPositiveButton(R.string.finish) { _, _ ->
                navigateBack()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        
        dialog.show()
        
        // Применяем стили к кнопкам после показа диалога
        val buttonTextColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dialog_button_text_color)
        val buttonTextSize = 14f // 14sp
        
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

    protected open fun copyImageToPrivateStorage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val context = context ?: return@launch
                val copiedFile = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: return@withContext null

                    val fileDir = File(context.filesDir, "playlist_covers")
                    if (!fileDir.exists()) {
                        fileDir.mkdirs()
                    }

                    val fileName = "cover_${System.currentTimeMillis()}.jpg"
                    val outputFile = File(fileDir, fileName)

                    val outputStream = FileOutputStream(outputFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    outputFile
                }

                copiedFile?.let { file ->
                    viewModel.updateCoverPath(file.absolutePath)
                }
            } catch (e: Exception) {
                android.util.Log.e("CreatePlaylistFragment", "Error copying image", e)
            }
        }
    }

    protected open fun showSnackbar(message: String) {
        val snackbarView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_snackbar, null)
        
        val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
        textView.text = message
        
        val snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        snackbar.view.alpha = 0f
        
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.removeAllViews()
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(snackbarView)
        
        val params = snackbarLayout.layoutParams as android.view.ViewGroup.MarginLayoutParams
        val screenWidth = resources.displayMetrics.widthPixels
        val leftMargin = resources.getDimensionPixelSize(R.dimen.snackbar_left_margin)
        val rightMargin = resources.getDimensionPixelSize(R.dimen.snackbar_right_margin)
        params.width = screenWidth - leftMargin - rightMargin
        params.height = resources.getDimensionPixelSize(R.dimen.snackbar_height)
        params.topMargin = resources.getDimensionPixelSize(R.dimen.snackbar_top_margin)
        params.leftMargin = leftMargin
        params.rightMargin = rightMargin
        snackbarLayout.layoutParams = params
        
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

