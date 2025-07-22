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

class SubscriptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        // Takaisin‐nuoli
        findViewById<MaterialToolbar>(R.id.toolbar_subs)
            .setNavigationOnClickListener { finish() }

        // Lisää/Muokkaa (TODO)
        findViewById<Button>(R.id.btn_add_sub)
            .setOnClickListener { /* TODO */ }
        findViewById<Button>(R.id.btn_edit_sub)
            .setOnClickListener { /* TODO */ }

        lifecycleScope.launch {
            val repository = FinanceRepository(this@SubscriptionsActivity)
            val transactions = repository.getRealTransactions().first()
            val list = transactions.filter { it.isMonthlyPayment }
            val container = findViewById<LinearLayout>(R.id.ll_container_subs)
            val inflater = LayoutInflater.from(this@SubscriptionsActivity)

            list.forEach { tx ->
                val card = inflater.inflate(R.layout.item_subscription_card, container, false)
                card.findViewById<TextView>(R.id.tv_subs_name).text = tx.name
                card.findViewById<TextView>(R.id.tv_subs_amount).text =
                    String.format(Locale.getDefault(), "%.2f €/kk", -tx.amount)
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                card.findViewById<TextView>(R.id.tv_subs_date).text = fmt.format(tx.paymentDate)
                card.findViewById<TextView>(R.id.tv_subs_due).text =
                    String.format(Locale.getDefault(), "Eräpäivä: %s", tx.dueDate?.let { fmt.format(it) } ?: "-")
                container.addView(card)
            }
        }
    }
}
