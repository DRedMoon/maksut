package com.oma.maksut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.databinding.ActivityUpcomingBinding

class UpcomingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpcomingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpcomingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // RecyclerViews setup
        binding.rvThisWeek.layoutManager = LinearLayoutManager(this)
        binding.rvThisMonth.layoutManager = LinearLayoutManager(this)
        binding.rvThisYear.layoutManager = LinearLayoutManager(this)
        // Adapters will be set up after data is loaded
    }
}