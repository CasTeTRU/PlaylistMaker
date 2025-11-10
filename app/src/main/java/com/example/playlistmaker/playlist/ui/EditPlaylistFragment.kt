package com.example.playlistmaker.playlist.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()
    private val editViewModel: EditPlaylistViewModel by viewModel()
    
    // Переопределяем viewModel, чтобы базовый класс использовал правильный тип
    override val viewModel: CreatePlaylistViewModel
        get() = editViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            // Не вызываем super.onViewCreated(), чтобы избежать проблем с инициализацией ViewModel
            // Вызываем методы базового класса вручную в правильном порядке
            setupViews()
            observeViewModel() // Вызываем переопределенную версию
            restoreState()
            // Загружаем плейлист при входе на экран
            editViewModel.loadPlaylist(args.playlistId)
        } catch (e: Exception) {
            android.util.Log.e("EditPlaylistFragment", "Error in onViewCreated", e)
            throw e
        }
    }

    override fun observeViewModel() {
        // Полностью переопределяем observeViewModel для режима редактирования
        // Не вызываем super, чтобы избежать двойной подписки и конфликтов с заголовком/кнопкой
        
        editViewModel.state.observe(viewLifecycleOwner) { state ->
            // Устанавливаем заголовок и текст кнопки для режима редактирования
            binding.titleTextView.text = getString(R.string.edit)
            binding.createButton.text = getString(R.string.save)

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

        // Подписываемся на обновление плейлиста
        editViewModel.isPlaylistUpdated.observe(viewLifecycleOwner) { isUpdated ->
            if (isUpdated) {
                if (isAdded && !isRemoving) {
                    navigateBack()
                }
            }
        }
    }

    override fun handleBackPress() {
        // В режиме редактирования при нажатии "Назад" просто возвращаемся без диалога
        navigateBack()
    }
}

