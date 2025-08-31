package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var themeSwitcher: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        val btnBack = findViewById<Button>(R.id.btn_back)
        themeSwitcher = findViewById(R.id.themeSwitcher)

        btnBack.setOnClickListener {
            finish()
        }

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked)
        }

        val shareAppButton = findViewById<Button>(R.id.share_app_button)
        shareAppButton.setOnClickListener {
            shareApp()
        }

        val contactSupportButton = findViewById<Button>(R.id.contact_support_button)
        contactSupportButton.setOnClickListener {
            contactSupport()
        }

        val termsOfServiceButton = findViewById<Button>(R.id.terms_of_service_button)
        termsOfServiceButton.setOnClickListener {
            openTermsOfService()
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            themeSwitcher.isChecked = state.isDarkTheme
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

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTermsOfService() {
        val url = getString(R.string.terms_of_service_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
        }
    }
}