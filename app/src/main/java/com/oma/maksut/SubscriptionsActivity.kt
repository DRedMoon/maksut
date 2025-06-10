package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale


class SubscriptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        // 1) “Close”-teksti sulkemaan tämän näkymän
        findViewById<TextView>(R.id.tv_close_subs)
            .setOnClickListener { finish() }

        // 2) Hae kaikki subscription-kategoriaan merkityt tapahtumat
        val subs = TransactionRepository.transactions
            .filter { it.category == Category.SUBSCRIPTION }

        // 3) Lisää kukin kortti scroll-kontaineriin
        val container = findViewById<LinearLayout>(R.id.ll_container_subs)
        val inflater  = LayoutInflater.from(this)

        subs.forEach { tx ->
            // Käytä samaa item_payment_card.xml-pohjaa
            val card = inflater.inflate(R.layout.item_payment_card, container, false)

            // Täytä kortin kentät
            card.findViewById<TextView>(R.id.tv_loan_name).text      = tx.label
            card.findViewById<TextView>(R.id.tv_loan_remaining).text =
                String.format(Locale.getDefault(), "%.2f €", -tx.amount)
            // Jos ei ole kuukausieriä/subscription-erittelyä, jätä tyhjäksi
            card.findViewById<TextView>(R.id.tv_loan_monthly).text   = ""
            card.findViewById<TextView>(R.id.tv_loan_rate).text      = ""

            // Lisää kortti kontaineriin
            container.addView(card)
        }
    }
}
