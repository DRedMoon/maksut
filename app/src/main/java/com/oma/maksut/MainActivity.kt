package com.oma.maksut

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val transactions = mutableListOf<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private var currentPage = 0 // 0=saldo, 1=lainat, 2=muu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar (Maksut)
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        // RecyclerView (Tapahtumat)
        adapter = TransactionAdapter(transactions)
        findViewById<RecyclerView>(R.id.rv_transactions).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // Testidata
        transactions.add(Transaction(R.drawable.ic_bank, getString(R.string.salary), 2500.0, "2025-06-08"))
        transactions.add(Transaction(R.drawable.ic_shopping, getString(R.string.expense), -75.50, "2025-06-08"))
        adapter.notifyItemRangeInserted(0, transactions.size)

        // Alustus: saldo & pisteet
        updateRemainingText()
        updatePageIndicator()

        // Klikkaamalla pisteitä vaihdetaan näkymää
        findViewById<LinearLayout>(R.id.ll_page_indicator).setOnClickListener {
            nextPage()
        }

        // Pyyhkäisyeleet vain headeriin (Jäljellä-palkkiin)
        setupSwipe(findViewById(R.id.ll_header))
        // Huom: EI enää setupSwipe(rv_transactions) – näin tapahtumat eivät swaippaa

        // Pieni '+'-nappi tapahtumat-otsikossa
        findViewById<ImageButton>(R.id.btn_add_transaction_small).setOnClickListener {
            showTransactionDialog()
        }
    }

    private fun nextPage() {
        currentPage = (currentPage + 1) % 3
        updatePageIndicator()
        updateRemainingText()
    }
    private fun prevPage() {
        currentPage = (currentPage + 2) % 3
        updatePageIndicator()
        updateRemainingText()
    }

    private fun updateRemainingText() {
        val tv = findViewById<TextView>(R.id.tv_remaining_amount)
        val text = when (currentPage) {
            0 -> String.format(Locale.getDefault(), "%.2f €", transactions.sumOf { it.amount })
            1 -> String.format(Locale.getDefault(), "%.2f €", -transactions.filter { it.amount < 0 }.sumOf { it.amount })
            else -> String.format(Locale.getDefault(), "%.2f €", transactions.filter { it.amount > 0 }.sumOf { it.amount })
        }
        tv.text = text
    }

    private fun updatePageIndicator() {
        val dots = listOf(R.id.iv_dot1, R.id.iv_dot2, R.id.iv_dot3)
        dots.forEachIndexed { idx, id ->
            findViewById<ImageView>(id)
                .setImageResource(if (idx == currentPage) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        }
    }

    @Suppress("ClickableViewAccessibility")
    private fun setupSwipe(view: View) {
        var startX = 0f
        val threshold = 50
        view.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> startX = ev.x
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    val delta = ev.x - startX
                    if (delta > threshold) prevPage()
                    else if (delta < -threshold) nextPage()
                }
            }
            true
        }
    }

    private fun showTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction, null)
        val etDesc   = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val btnDate  = dialogView.findViewById<Button>(R.id.btn_date_picker)
        val tvDate   = dialogView.findViewById<TextView>(R.id.tv_selected_date)

        btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    tvDate.text = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_transaction)
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                val descVal   = etDesc.text.toString()
                val amountVal = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val dateVal   = tvDate.text.toString()
                transactions.add(
                    Transaction(
                        iconRes = if (amountVal >= 0) R.drawable.ic_bank else R.drawable.ic_shopping,
                        label   = descVal,
                        amount  = amountVal,
                        time    = dateVal
                    )
                )
                adapter.notifyItemInserted(transactions.size - 1)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
