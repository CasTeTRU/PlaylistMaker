package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

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

