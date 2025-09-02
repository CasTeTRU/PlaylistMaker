package com.example.playlistmaker.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.databinding.ActivityMainBinding
import com.example.playlistmaker.library.ui.MediatekaActivity
import com.example.playlistmaker.search.ui.SearchActivity
import com.example.playlistmaker.settings.ui.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnSearch.setOnClickListener {
            openSearchScreen()
        }

        binding.btnLibrary.setOnClickListener {
            openLibraryScreen()
        }

        binding.btnSettings.setOnClickListener {
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