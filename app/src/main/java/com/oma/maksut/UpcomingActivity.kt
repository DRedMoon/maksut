package com.oma.maksut

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.adapter.TransactionAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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

        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)
                repo.getUpcomingTransactions(/*start*/Date(), /*end*/Date()).collect { transactions ->
                    // TODO: Filter transactions for week/month/year
                    weekAdapter.updateItems(transactions)
                    monthAdapter.updateItems(transactions)
                    yearAdapter.updateItems(transactions)
                }
            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading upcoming transactions", e)
            }
        }
    }
}