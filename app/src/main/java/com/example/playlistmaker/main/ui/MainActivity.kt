package com.example.playlistmaker.main.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.library.MediatekaActivity
import com.example.playlistmaker.search.ui.SearchActivity
import com.example.playlistmaker.settings.ui.SettingsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnLibrary = findViewById<Button>(R.id.btnLibrary)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnSearch.setOnClickListener {
            openSearchScreen()
        }

        btnLibrary.setOnClickListener {
            openLibraryScreen()
        }

        btnSettings.setOnClickListener {
            openSettingsScreen()
        }
    }

    private fun openSearchScreen() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    private fun openLibraryScreen() {
        val intent = Intent(this, MediatekaActivity::class.java)
        startActivity(intent)
    }

    private fun openSettingsScreen() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}