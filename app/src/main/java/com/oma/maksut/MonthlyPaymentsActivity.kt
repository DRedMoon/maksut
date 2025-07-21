package com.oma.maksut

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class MonthlyPaymentsActivity : AppCompatActivity() {
    private lateinit var tvTotal: TextView
    private lateinit var rvPayments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_payments)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tvTotal = findViewById(R.id.tv_monthly_payments_total)
        rvPayments = findViewById(R.id.rv_monthly_payments)
        rvPayments.layoutManager = LinearLayoutManager(this)

        // Toggle list visibility on total click
        tvTotal.setOnClickListener {
            rvPayments.visibility = if (rvPayments.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // TODO: Set up adapter and load monthly payments data
    }
}