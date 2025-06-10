package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        findViewById<MaterialToolbar>(R.id.toolbar_credits)
            .setNavigationOnClickListener { finish() }

        val credits = TransactionRepository.transactions
            .filter { it.category == Category.EXPENSE }

        val container = findViewById<LinearLayout>(R.id.ll_container_credits)
        val inflater  = LayoutInflater.from(this)
        credits.forEach { tx ->
            val card = inflater.inflate(R.layout.item_payment_card, container, false)
            card.findViewById<TextView>(R.id.tv_loan_name).text      = tx.label
            card.findViewById<TextView>(R.id.tv_loan_remaining).text = String.format(
                Locale.getDefault(), "%.2f €", -tx.amount)
            card.findViewById<TextView>(R.id.tv_loan_monthly).text   = ""  // ei kk‐eriä
            card.findViewById<TextView>(R.id.tv_loan_rate).text      = ""
            container.addView(card)
        }
    }
}
