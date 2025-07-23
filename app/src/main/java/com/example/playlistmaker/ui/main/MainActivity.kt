package com.example.playlistmaker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.ui.library.MediatekaActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.search.SearchActivity
import com.example.playlistmaker.presentation.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_search = findViewById<Button>(R.id.btnSearch)
        val btn_library = findViewById<Button>(R.id.btnLibrary)
        val btn_settings = findViewById<Button>(R.id.btnSettings)

btn_search.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
}

        val btnLibraryClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                val mediatekaIntent = Intent(this@MainActivity, MediatekaActivity::class.java)
                startActivity(mediatekaIntent)
            }
        }

btn_library.setOnClickListener(btnLibraryClickListener)


btn_settings.setOnClickListener {
    val settingsIntent = Intent(this, SettingsActivity::class.java)
    startActivity(settingsIntent)
        }
    }
}