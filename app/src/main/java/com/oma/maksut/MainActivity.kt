package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // Alustus luokan ulkopuolella
    private var balance = 0.0
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private var currentPage = 0   // 0 = saldo, 1 = lainat, 2 = muu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView ja adapter
        val rv = findViewById<RecyclerView>(R.id.rv_transactions)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactions)
        rv.adapter = adapter

        // Saldo-teksti
        val remainingTv = findViewById<TextView>(R.id.tv_remaining_amount)
        remainingTv.text = String.format(Locale.getDefault(), "%.2f €", balance)

      //  val dot1 = findViewById<ImageView>(R.id.iv_dot1)
      //  val dot2 = findViewById<ImageView>(R.id.iv_dot2)
      //  val dot3 = findViewById<ImageView>(R.id.iv_dot3)
        updatePageIndicator()

        findViewById<LinearLayout>(R.id.ll_page_indicator).setOnClickListener {
            // Kierto 0→1→2→0
            currentPage = (currentPage + 1) % 3
            when (currentPage) {
                0 -> remainingTv.text = String.format(Locale.getDefault(), "%.2f €", balance)
                1 -> {
                    val loansAmount = "3 500 €"  // TÄMÄ PITÄISI OLLA MÄÄRITELTYÄ KODISSA
                    remainingTv.text = getString(R.string.loans_template, loansAmount)
                }
                2 -> remainingTv.text = getString(R.string.third_page)      // esim. muu näkymä
            }
            updatePageIndicator()
        }

        // Lisää palkka -nappi
        findViewById<Button>(R.id.btn_add_salary).setOnClickListener {
            showAmountDialog(isSalary = true, remainingTv)
        }

        // Lisää kulu -nappi
        findViewById<Button>(R.id.btn_add_expense).setOnClickListener {
            showAmountDialog(isSalary = false, remainingTv)
        }

        // Asetukset-ikoni
        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    private fun updatePageIndicator() {
        // Täytetty (filled) = currentPage, muut outline
        findViewById<ImageView>(R.id.iv_dot1).setImageResource(
            if (currentPage == 0) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline
        )
        findViewById<ImageView>(R.id.iv_dot2).setImageResource(
            if (currentPage == 1) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline
        )
        findViewById<ImageView>(R.id.iv_dot3).setImageResource(
            if (currentPage == 2) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline
        )
    }
    private fun showAmountDialog(isSalary: Boolean, remainingTv: TextView) {
        val title = if (isSalary) getString(R.string.add_salary) else getString(R.string.add_expense)
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "0.00"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                val text = input.text.toString()
                val amount = text.toDoubleOrNull() ?: 0.0

                // Päivitä saldo
                balance += if (isSalary) amount else -amount
                remainingTv.text = String.format(Locale.getDefault(), "%.2f €", balance)

                // Lisää tapahtuma listalle
                val icon = if (isSalary) R.drawable.ic_bank else R.drawable.ic_shopping
                val label = if (isSalary) getString(R.string.salary) else getString(R.string.expense)
                transactions.add(0, Transaction(icon, label, if (isSalary) amount else -amount))
                adapter.notifyItemInserted(0)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
