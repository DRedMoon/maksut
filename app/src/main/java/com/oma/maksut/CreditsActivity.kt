package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        // Takaisin‐nuoli
        findViewById<MaterialToolbar>(R.id.toolbar_credits)
            .setNavigationOnClickListener { finish() }

        // Lisää / Muokkaa (TODO)
        findViewById<Button>(R.id.btn_add_credits).setOnClickListener { /* TODO */ }
        findViewById<Button>(R.id.btn_edit_credits).setOnClickListener { /* TODO */ }

        // Korttien luonti
        val list      = TransactionRepository.transactions.filter { it.category == Category.EXPENSE }
        val container = findViewById<LinearLayout>(R.id.ll_container_credits)
        val inflater  = LayoutInflater.from(this)

        list.forEach { tx ->
            val card = inflater.inflate(R.layout.item_credit_card, container, false)
            card.findViewById<TextView>(R.id.tv_credit_name).text      = tx.label
            card.findViewById<TextView>(R.id.tv_credit_remaining).text =
                String.format(Locale.getDefault(), "%.2f €", -tx.amount)
            card.findViewById<TextView>(R.id.tv_credit_monthly).text   =
                String.format(Locale.getDefault(), "%.2f €/kk", tx.monthlyPayment)
            card.findViewById<TextView>(R.id.tv_credit_rate).text      =
                String.format(Locale.getDefault(), "Korko %.2f%%", tx.rate)
            card.findViewById<TextView>(R.id.tv_credit_fee).text       =
                String.format(Locale.getDefault(), "Palkkio %.2f€", tx.fee)
            card.findViewById<TextView>(R.id.tv_credit_due).text       =
                String.format(Locale.getDefault(), "Eräpäivä ${tx.dueDate}")
            container.addView(card)
        }
    }
}
