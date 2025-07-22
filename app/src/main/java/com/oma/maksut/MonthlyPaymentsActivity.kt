package com.oma.maksut

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import com.oma.maksut.repository.FinanceRepository
import com.oma.maksut.database.entities.Transaction

class MonthlyPaymentsActivity : AppCompatActivity() {
    private lateinit var tvTotal: TextView
    private lateinit var rvPayments: RecyclerView
    private lateinit var adapter: MonthlyPaymentsAdapter
    private var allPayments: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_payments)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tvTotal = findViewById(R.id.tv_monthly_payments_total)
        rvPayments = findViewById(R.id.rv_monthly_payments)
        rvPayments.layoutManager = LinearLayoutManager(this)

        adapter = MonthlyPaymentsAdapter(emptyList()) { tx ->
            togglePaidStatus(tx)
        }
        rvPayments.adapter = adapter

        // Toggle list visibility on total click
        tvTotal.setOnClickListener {
            rvPayments.visibility = if (rvPayments.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Load monthly payments
        lifecycleScope.launch {
            try {
                val repo = FinanceRepository(this@MonthlyPaymentsActivity)
                repo.getMonthlyPayments().collect { payments ->
                    try {
                        allPayments = payments
                        adapter.updateItems(payments)
                        val total = payments.sumOf { it.amount }
                        tvTotal.text = String.format(Locale.getDefault(), "%.2f €", total)
                    } catch (e: Exception) {
                        android.util.Log.e("MonthlyPaymentsActivity", "Error processing payments", e)
                        tvTotal.text = "0.00 €"
                    }
                }
            } catch (e: Exception) {
                // Handle error gracefully
                tvTotal.text = "0.00 €"
                android.util.Log.e("MonthlyPaymentsActivity", "Error loading payments", e)
            }
        }

        // Progressive loading: show more on scroll to bottom
        rvPayments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 1 && adapter.itemCount < allPayments.size) {
                    adapter.showMore()
                }
            }
        })
    }

    private fun togglePaidStatus(tx: Transaction) {
        lifecycleScope.launch {
            try {
                val repo = FinanceRepository(this@MonthlyPaymentsActivity)
                repo.updatePaymentStatus(tx.id, !tx.isPaid)
            } catch (e: Exception) {
                android.util.Log.e("MonthlyPaymentsActivity", "Error updating payment status", e)
            }
        }
    }
}