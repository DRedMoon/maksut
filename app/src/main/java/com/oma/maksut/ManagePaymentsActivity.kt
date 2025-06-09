package com.oma.maksut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class ManagePaymentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_payments)
        supportActionBar?.title = getString(R.string.manage_payments)

        findViewById<TextView>(R.id.tv_close_settings)
            .setOnClickListener { finish() }

        // 1) Suodatus: valitaan vain lainakategoriassa olevat
        val loans = TransactionRepository.transactions
            .filter { it.category == Category.LOAN }

        // 2) Näytetään ne RecyclerViewissä
        findViewById<RecyclerView>(R.id.rv_loans).apply {
            layoutManager = LinearLayoutManager(this@ManagePaymentsActivity)
            adapter = TransactionAdapter(loans)
        }

        // Sulje tämä activity, kun X-painiketta painetaan
        findViewById<TextView>(R.id.tv_close_settings)
            .setOnClickListener { finish() }
    }
}