package com.example.playlistmaker.library

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.R

class MediatekaActivity : AppCompatActivity() {

    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateka)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        imageView = findViewById(R.id.image)
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            Glide.with(this)
                .load(state.imageUrl)
                .into(imageView)
        }
    }
}
