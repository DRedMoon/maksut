package com.oma.maksut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class UpcomingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvThisWeek = findViewById<RecyclerView>(R.id.rv_this_week)
        val rvThisMonth = findViewById<RecyclerView>(R.id.rv_this_month)
        val rvThisYear = findViewById<RecyclerView>(R.id.rv_this_year)
        rvThisWeek.layoutManager = LinearLayoutManager(this)
        rvThisMonth.layoutManager = LinearLayoutManager(this)
        rvThisYear.layoutManager = LinearLayoutManager(this)
        // TODO: Set adapters and load data
    }
}