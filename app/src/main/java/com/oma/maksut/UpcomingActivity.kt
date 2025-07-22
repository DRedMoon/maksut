package com.oma.maksut

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.TransactionAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.Date

class UpcomingActivity : AppCompatActivity() {
    private lateinit var weekAdapter: TransactionAdapter
    private lateinit var monthAdapter: TransactionAdapter
    private lateinit var yearAdapter: TransactionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming)

        try {
            val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { finish() }
        } catch (e: Exception) {
            android.util.Log.e("UpcomingActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error loading upcoming", Toast.LENGTH_SHORT).show()
            finish()
        }

        try {
            val rvThisWeek = findViewById<RecyclerView>(R.id.rv_this_week)
            val rvThisMonth = findViewById<RecyclerView>(R.id.rv_this_month)
            val rvThisYear = findViewById<RecyclerView>(R.id.rv_this_year)
            rvThisWeek.layoutManager = LinearLayoutManager(this)
            rvThisMonth.layoutManager = LinearLayoutManager(this)
            rvThisYear.layoutManager = LinearLayoutManager(this)

            weekAdapter = TransactionAdapter(emptyList())
            monthAdapter = TransactionAdapter(emptyList())
            yearAdapter = TransactionAdapter(emptyList())
            rvThisWeek.adapter = weekAdapter
            rvThisMonth.adapter = monthAdapter
            rvThisYear.adapter = yearAdapter
        } catch (e: Exception) {
            android.util.Log.e("UpcomingActivity", "Error setting up RecyclerViews", e)
            Toast.makeText(this, "Error setting up upcoming", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup filter button clicks
        findViewById<Button>(R.id.btn_this_week).setOnClickListener {
            showView(R.id.ll_this_week)
            loadThisWeekData()
        }
        
        findViewById<Button>(R.id.btn_month).setOnClickListener {
            showView(R.id.ll_month)
            loadMonthData()
        }
        
        findViewById<Button>(R.id.btn_year).setOnClickListener {
            showView(R.id.ll_year)
            loadYearData()
        }
        
        // Start with "This week" view
        showView(R.id.ll_this_week)
        loadThisWeekData()
    }
    
    private fun showView(viewId: Int) {
        // Hide all views
        findViewById<LinearLayout>(R.id.ll_this_week).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.ll_month).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.ll_year).visibility = android.view.View.GONE
        
        // Show selected view
        findViewById<LinearLayout>(viewId).visibility = android.view.View.VISIBLE
    }
    
    private fun loadThisWeekData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)
                // TODO: Filter for this week (Monday to Sunday)
                val transactions = repo.getUpcomingTransactions(Date(), Date()).first()
                weekAdapter.updateItems(transactions)
            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading this week data", e)
            }
        }
    }
    
    private fun loadMonthData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)
                // TODO: Filter for this month
                val transactions = repo.getUpcomingTransactions(Date(), Date()).first()
                monthAdapter.updateItems(transactions)
            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading month data", e)
            }
        }
    }
    
    private fun loadYearData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)
                // TODO: Filter for this year
                val transactions = repo.getUpcomingTransactions(Date(), Date()).first()
                yearAdapter.updateItems(transactions)
            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading year data", e)
            }
        }
    }
}