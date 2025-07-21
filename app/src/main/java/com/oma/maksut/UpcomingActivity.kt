package com.oma.maksut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Transaction
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date

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

        val weekAdapter = TransactionAdapter(emptyList())
        val monthAdapter = TransactionAdapter(emptyList())
        val yearAdapter = TransactionAdapter(emptyList())
        rvThisWeek.adapter = weekAdapter
        rvThisMonth.adapter = monthAdapter
        rvThisYear.adapter = yearAdapter

        lifecycleScope.launch {
            val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)
            repo.getUpcomingTransactions(/*start*/Date(), /*end*/Date()).collect { transactions ->
                // TODO: Filter transactions for week/month/year
                weekAdapter.updateItems(transactions)
                monthAdapter.updateItems(transactions)
                yearAdapter.updateItems(transactions)
            }
        }
    }
}