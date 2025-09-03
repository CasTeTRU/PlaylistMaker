package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.main.ui.MainActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by inject()
    private lateinit var localBroadcastManager: LocalBroadcastManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            localBroadcastManager = LocalBroadcastManager.getInstance(requireContext())
            setupViews()
            observeViewModel()
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error in onViewCreated", e)
        }
    }

    private fun setupViews() {
        try {
            // Свитчер темной темы
            binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
                try {
                    Log.d("SettingsFragment", "Theme switch changed to: $isChecked")
                    viewModel.toggleTheme(isChecked)
                    
                    // Отправляем broadcast для уведомления MainActivity
                    val intent = Intent(MainActivity.ACTION_THEME_CHANGED)
                    intent.putExtra(MainActivity.EXTRA_IS_DARK_THEME, isChecked)
                    localBroadcastManager.sendBroadcast(intent)
                    
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error toggling theme", e)
                }
            }

            // Кнопка "Поделиться приложением"
            binding.shareAppButton.setOnClickListener {
                try {
                    shareApp()
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error sharing app", e)
                }
            }

            // Кнопка "Написать в поддержку"
            binding.contactSupportButton.setOnClickListener {
                try {
                    contactSupport()
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error contacting support", e)
                }
            }

            // Кнопка "Пользовательское соглашение"
            binding.termsOfServiceButton.setOnClickListener {
                try {
                    openTermsOfService()
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error opening terms", e)
                }
                }
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error setting up views", e)
        }
    }

    private fun shareApp() {
        val shareMessage = getString(R.string.share_app_message, getString(R.string.share_app_url))
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(intent, "Поделиться через"))
    }

    private fun contactSupport() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.support_subject)
        val body = getString(R.string.support_body)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTermsOfService() {
        val url = getString(R.string.terms_of_service_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Браузер не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.state.observe(viewLifecycleOwner) { state ->
                try {
                    Log.d("SettingsFragment", "Theme state updated: ${state.isDarkTheme}")
                    binding.switchTheme.isChecked = state.isDarkTheme
                } catch (e: Exception) {
                    Log.e("SettingsFragment", "Error updating UI", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Error observing ViewModel", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
