package com.example.playlistmaker.ui.library

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R

class MediatekaActivity : Activity() {

    private val imageUrl = "https://img.freepik.com/free-vector/open-blue-book-white_1308-69339.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateka)

        val image = findViewById<ImageView>(R.id.image)

        Glide.with(applicationContext).load(imageUrl).into(image)
    }
}



