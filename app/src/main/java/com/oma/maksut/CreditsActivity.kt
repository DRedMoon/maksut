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
import com.oma.maksut.repository.FinanceRepository
import java.text.SimpleDateFormat

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

        lifecycleScope.launch {
            val repository = FinanceRepository(this@CreditsActivity)
            val credits = repository.getAllActiveCredits().first()
            val container = findViewById<LinearLayout>(R.id.ll_container_credits)
            val inflater = LayoutInflater.from(this@CreditsActivity)

            credits.forEach { credit ->
                val card = inflater.inflate(R.layout.item_credit_card, container, false)
                card.findViewById<TextView>(R.id.tv_credit_name).text = credit.name
                card.findViewById<TextView>(R.id.tv_credit_remaining).text =
                    String.format(Locale.getDefault(), "%.2f €", credit.currentBalance)
                card.findViewById<TextView>(R.id.tv_credit_monthly).text =
                    String.format(Locale.getDefault(), "%.2f €/kk", credit.minPaymentAmount)
                card.findViewById<TextView>(R.id.tv_credit_rate).text =
                    String.format(Locale.getDefault(), "Korko %.2f%%", credit.totalInterestRate)
                card.findViewById<TextView>(R.id.tv_credit_fee).text =
                    String.format(Locale.getDefault(), "Palkkio %.2f€", 0.0) // Credits don't have fees in the model
                card.findViewById<TextView>(R.id.tv_credit_due).text =
                    String.format(Locale.getDefault(), "Eräpäivä %s", credit.dueDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it) } ?: "-")
                container.addView(card)
            }
        }
    }
}
