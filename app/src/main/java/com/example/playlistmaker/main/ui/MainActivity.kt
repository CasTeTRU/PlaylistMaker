package com.example.playlistmaker.main.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var localBroadcastManager: LocalBroadcastManager

    companion object {
        const val ACTION_THEME_CHANGED = "com.example.playlistmaker.THEME_CHANGED"
        const val EXTRA_IS_DARK_THEME = "is_dark_theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем текущую тему перед установкой layout
        applyCurrentTheme()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupThemeBroadcastReceiver()
        
        // Устанавливаем начальный заголовок
        binding.screenTitle.text = getString(R.string.library)
    }

    private fun applyCurrentTheme() {
        // Получаем текущую тему из SharedPreferences
        val prefs = getSharedPreferences("playlist_maker_storage", MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean("dark_theme", false)
        android.util.Log.d("MainActivity", "Current theme from prefs: isDarkTheme = $isDarkTheme")
        
        // Применяем тему
        val mode = if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        android.util.Log.d("MainActivity", "Setting initial night mode to: $mode")
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Связываем BottomNavigationView с NavController
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Устанавливаем цвета для BottomNavigationView
        updateBottomNavigationColors()
        
        // Настраиваем обновление заголовка при изменении экрана
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateScreenTitle(destination.label?.toString())
        }
    }

    private fun setupThemeBroadcastReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        
        val filter = IntentFilter(ACTION_THEME_CHANGED)
        localBroadcastManager.registerReceiver(themeChangedReceiver, filter)
    }

    private val themeChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_THEME_CHANGED) {
                val isDarkTheme = intent.getBooleanExtra(EXTRA_IS_DARK_THEME, false)
                android.util.Log.d("MainActivity", "Theme changed to dark: $isDarkTheme")
                applyTheme(isDarkTheme)
                // Обновляем UI без пересоздания активности
                updateUIForTheme()
            }
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        android.util.Log.d("MainActivity", "Applying theme: isDarkTheme = $isDarkTheme")
        val mode = if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        android.util.Log.d("MainActivity", "Setting night mode to: $mode")
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun updateUIForTheme() {
        // Обновляем цвета BottomNavigationView
        updateBottomNavigationColors()
        
        // Принудительно обновляем BottomNavigationView
        binding.bottomNavigation.invalidate()
        
        // Обновляем заголовок экрана
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentDestination = navHostFragment.navController.currentDestination
        updateScreenTitle(currentDestination?.label?.toString())
        
        android.util.Log.d("MainActivity", "UI updated for theme")
    }

    private fun updateBottomNavigationColors() {
        // Устанавливаем цвета для BottomNavigationView в зависимости от темы
        val colorStateList = resources.getColorStateList(R.color.bottom_navigation_colors, theme)
        binding.bottomNavigation.itemIconTintList = colorStateList
        binding.bottomNavigation.itemTextColor = colorStateList
    }

    private fun updateScreenTitle(title: String?) {
        title?.let {
            binding.screenTitle.text = it
            android.util.Log.d("MainActivity", "Screen title updated to: $it")
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем UI при возобновлении активности
        updateUIForTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(themeChangedReceiver)
    }
}