package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<Button>(R.id.btn_back)

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

