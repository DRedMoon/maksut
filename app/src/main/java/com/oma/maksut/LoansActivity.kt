package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class LoansActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loans)

        // Takaisin-painike
        findViewById<MaterialToolbar>(R.id.toolbar_loans)
            .setNavigationOnClickListener { finish() }

        // Hae lainat ja renderöi kortit
        val loans = TransactionRepository.transactions
            .filter { it.category == Category.LOAN }
        val container = findViewById<LinearLayout>(R.id.ll_container_loans)
        val inflater  = LayoutInflater.from(this)
        loans.forEach { loan ->
            val card = inflater.inflate(R.layout.item_payment_card, container, false)
            card.findViewById<TextView>(R.id.tv_loan_name).text      = loan.label
            card.findViewById<TextView>(R.id.tv_loan_remaining).text =
                String.format(Locale.getDefault(), "%.2f €", -loan.amount)
            card.findViewById<TextView>(R.id.tv_loan_monthly).text   =
                String.format(Locale.getDefault(), "%.2f €/kk", loan.amount)
            card.findViewById<TextView>(R.id.tv_loan_rate).text      =
                "Korko ${loan.rate}%"
            container.addView(card)
        }
    }
}
