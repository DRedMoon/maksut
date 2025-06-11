package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class OthersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others)

        // Takaisin‐nuoli
        findViewById<MaterialToolbar>(R.id.toolbar_others)
            .setNavigationOnClickListener { finish() }

        // Lisää/Muokkaa muut
        findViewById<Button>(R.id.btn_add_other)
            .setOnClickListener { /* TODO */ }
        findViewById<Button>(R.id.btn_edit_other)
            .setOnClickListener { /* TODO */ }

        val list      = TransactionRepository.transactions.filter { it.category == Category.OTHER }
        val container = findViewById<LinearLayout>(R.id.ll_container_others)
        val inflater  = LayoutInflater.from(this)

        list.forEach { tx ->
            val card = inflater.inflate(R.layout.item_other_card, container, false)
            card.findViewById<TextView>(R.id.tv_other_name).text   = tx.label
            card.findViewById<TextView>(R.id.tv_other_amount).text =
                String.format(Locale.getDefault(), "%.2f €", tx.amount)
            card.findViewById<TextView>(R.id.tv_other_date).text   = tx.time
            container.addView(card)
        }
    }
}
