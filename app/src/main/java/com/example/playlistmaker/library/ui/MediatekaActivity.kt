package com.example.playlistmaker.library.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import com.example.playlistmaker.databinding.ActivityMediatekaBinding
import com.google.android.material.tabs.TabLayoutMediator

class MediatekaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediatekaBinding
    private lateinit var pagerAdapter: MediatekaViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediatekaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupViewPager()
        setupTabLayout()
        setupBackPress()
        observeViewModel()
    }

    private fun setupViews() {
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = MediatekaViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = pagerAdapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(com.example.playlistmaker.R.string.favorite_tracks)
                1 -> getString(com.example.playlistmaker.R.string.playlists)
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
