package com.example.playlistmaker.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.data.dto.ThemeManager
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settingsInteractor = Creator.provideSettingsInteractor(this)
        val themeManager = ThemeManager(this)

        val btnBack = findViewById<Button>(R.id.btn_back)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        themeSwitcher.isChecked = settingsInteractor.isDarkTheme()

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            settingsInteractor.setDarkTheme(isChecked)
            themeManager.applyTheme(isChecked)
        }

        btnBack.setOnClickListener {
            finish()
        }

        val shareAppButton = findViewById<Button>(R.id.share_app_button)
        shareAppButton.setOnClickListener {
            val shareMessage = getString(R.string.share_app_message, getString(R.string.share_app_url))
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(intent, "Поделиться через"))
        }

        val contactSupportButton = findViewById<Button>(R.id.contact_support_button)
        contactSupportButton.setOnClickListener {
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

        val termsOfServiceButton = findViewById<Button>(R.id.terms_of_service_button)
        termsOfServiceButton.setOnClickListener {
            val url = getString(R.string.terms_of_service_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
            }
        }
    }
}