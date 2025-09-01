package com.example.playlistmaker.library.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.appbar.MaterialToolbar

class MediatekaActivity : AppCompatActivity() {

    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var btnBack: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: MediatekaViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateka)
        
        setupViews()
        setupViewPager()
        setupTabLayout()
        setupBackPress()
        observeViewModel()
    }

    private fun setupViews() {
        btnBack = findViewById(R.id.toolBar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        
        btnBack.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = MediatekaViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.playlists)
                1 -> getString(R.string.favorite_tracks)
                else -> ""
            }
        }.attach()
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun observeViewModel() {
        }
}
