package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.oma.maksut.repository.FinanceRepository
import java.text.SimpleDateFormat

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

        lifecycleScope.launch {
            val repository = FinanceRepository(this@OthersActivity)
            val transactions = repository.getRealTransactions().first()
            val list = transactions.filter { it.categoryId == 3L } // Assuming 3L is for OTHER category
            val container = findViewById<LinearLayout>(R.id.ll_container_others)
            val inflater = LayoutInflater.from(this@OthersActivity)

            list.forEach { tx ->
                val card = inflater.inflate(R.layout.item_other_card, container, false)
                card.findViewById<TextView>(R.id.tv_other_name).text = tx.name
                card.findViewById<TextView>(R.id.tv_other_amount).text =
                    String.format(Locale.getDefault(), "%.2f €", tx.amount)
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                card.findViewById<TextView>(R.id.tv_other_date).text = fmt.format(tx.paymentDate)
                container.addView(card)
            }
        }
    }
}
