package com.example.playlistmaker.playlist.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateCoverUri(uri)
            copyImageToPrivateStorage(uri)
        }
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

        setupViews()
        observeViewModel()
        // Восстанавливаем состояние при первом создании view
        restoreState()
    }

    private fun restoreState() {
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

    private fun setupViews() {
        binding.headerContainer.setOnClickListener {
            handleBackPress()
        }

        binding.coverContainer.setOnClickListener {
            pickImageLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Создаём TextWatcher для поля имени, избегая зацикливания при восстановлении состояния
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newText = s?.toString() ?: ""
                if (viewModel.state.value?.name != newText) {
                    viewModel.updateName(newText)
                }
            }
        }
        binding.nameEditText.addTextChangedListener(nameWatcher)
        binding.nameEditText.tag = nameWatcher // Сохраняем ссылку для возможного удаления

        // Создаём TextWatcher для поля описания, избегая зацикливания при восстановлении состояния
        val descriptionWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newText = s?.toString() ?: ""
                if (viewModel.state.value?.description != newText) {
                    viewModel.updateDescription(newText)
                }
            }
        }
        binding.descriptionEditText.addTextChangedListener(descriptionWatcher)
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

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
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
                binding.coverPlaceholder.visibility = View.GONE
            } else {
                binding.coverImageView.setImageDrawable(null)
                binding.coverPlaceholder.visibility = View.VISIBLE
            }
        }

        viewModel.isPlaylistCreated.observe(viewLifecycleOwner) { playlistName ->
            playlistName?.let {
                showSnackbar(getString(R.string.playlist_created, it))
                // Откладываем навигацию, чтобы Snackbar успел показаться и фрагмент был в правильном состоянии
                view?.postDelayed({
                    if (isAdded && !isRemoving) {
                        navigateBack()
                    }
                }, 2000) // Snackbar показывается дольше, чем Toast
            }
        }
    }

    private fun handleBackPress() {
        if (viewModel.hasUnsavedChanges()) {
            showConfirmDialog()
        } else {
            navigateBack()
        }
    }

    private fun navigateBack() {
        if (!isAdded || isRemoving) {
            return // Фрагмент уже удаляется или не добавлен
        }
        
        val activity = this.activity
        if (activity == null) {
            return
        }
        
        // Проверяем, есть ли мы в back stack FragmentManager (это значит, что мы в Activity)
        // Используем parentFragmentManager, так как это FragmentManager Activity
        val isInFragmentManager = try {
            parentFragmentManager.backStackEntryCount > 0
        } catch (e: Exception) {
            false
        }
        
        if (isInFragmentManager) {
            // Если мы в Activity (PlayerActivity), используем FragmentManager
            try {
                parentFragmentManager.popBackStack()
                // Скрываем контейнер после того, как транзакция завершена
                view?.post {
                    activity.findViewById<View>(R.id.fragment_container_create_playlist)?.visibility = View.GONE
                }
            } catch (e: IllegalStateException) {
                android.util.Log.e("CreatePlaylistFragment", "FragmentManager in illegal state", e)
                // Пытаемся скрыть контейнер напрямую
                activity.findViewById<View>(R.id.fragment_container_create_playlist)?.visibility = View.GONE
            } catch (e: Exception) {
                android.util.Log.e("CreatePlaylistFragment", "Error navigating back via FragmentManager", e)
            }
        } else {
            // Иначе используем Navigation (если мы в Navigation графе)
            try {
                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.createPlaylistFragment) {
                    navController.popBackStack()
                }
            } catch (e: IllegalStateException) {
                // Если Navigation недоступна, возможно мы в Activity
                // Попробуем использовать FragmentManager как fallback
                try {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                        activity.findViewById<View>(R.id.fragment_container_create_playlist)?.visibility = View.GONE
                    }
                } catch (ex: Exception) {
                    android.util.Log.e("CreatePlaylistFragment", "Error navigating back", ex)
                }
            } catch (e: Exception) {
                android.util.Log.e("CreatePlaylistFragment", "Error navigating back via Navigation", e)
            }
        }
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_playlist_creation)
            .setMessage(R.string.unsaved_data_warning)
            .setPositiveButton(R.string.finish) { _, _ ->
                navigateBack()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun copyImageToPrivateStorage(uri: Uri) {
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

    private fun showSnackbar(message: String) {
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
        val leftMargin = (7 * resources.displayMetrics.density).toInt()
        val rightMargin = (8 * resources.displayMetrics.density).toInt()
        params.width = screenWidth - leftMargin - rightMargin
        params.height = (48 * resources.displayMetrics.density).toInt()
        params.topMargin = (736 * resources.displayMetrics.density).toInt()
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

