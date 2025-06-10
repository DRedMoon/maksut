package com.oma.maksut

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat

// 0) Luokan kentät: suodatus-enum, formaatit, nykyinen suodatin
class MainActivity : AppCompatActivity() {
    private val transaction get() = TransactionRepository.transactions
    private lateinit var adapter: TransactionAdapter

    // 0a) Sivut 0=saldo,1=lainat,2=muu
    private var currentPage = 0

    // 0b) Suodatus enum ja muuttuja
    private enum class Filter { ALL, UPCOMING }
    private var currentFilter = Filter.ALL

    // 0c) Päivämääräformaatit
    private val fmtIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fmtDot = SimpleDateFormat("dd.M.yyyy",    Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Toolbar (Maksut + rattaan/valikon ikonit)
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        // 2) RecyclerView (Tapahtumat)
        adapter = TransactionAdapter(TransactionRepository.transactions)
        findViewById<RecyclerView>(R.id.rv_transactions).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // 3) Testidata
        val sampleDate = getString(R.string.sample_date)
        TransactionRepository.transactions += listOf(
            Transaction(
                iconRes  = R.drawable.ic_bank,
                label    = getString(R.string.salary),
                amount   = +2500.0,
                time     = sampleDate,
                category = Category.INCOME
            ),
            Transaction(
                iconRes  = R.drawable.ic_shopping,
                label    = getString(R.string.expense),
                amount   = -75.50,
                time     = sampleDate,
                category = Category.EXPENSE
            ),
            Transaction(
                iconRes  = R.drawable.ic_loan,
                label    = getString(R.string.loan_payment),
                amount   = -640.0,
                time     = sampleDate,
                rate     = 1.5,
                category = Category.LOAN
            ),
            Transaction(
                iconRes  = R.drawable.ic_subscription,
                label    = "Netflix",
                amount   = -12.99,
                time     = sampleDate,
                category = Category.SUBSCRIPTION
            )
        )
        adapter.notifyItemRangeInserted(0, TransactionRepository.transactions.size)

        // 4) Alku-näkymä: saldon ja indikaattorin päivitys
        updateRemainingText()
        updatePageIndicator()

        // Klikkaa summaa → näytä luotot / tilaukset eriteltynä
        findViewById<TextView>(R.id.tv_remaining_amount).setOnClickListener {
            when (currentPage) {
                1 -> showDetailDialog("Lainat ja luotot", TransactionRepository.transactions.filter { it.category == Category.LOAN })
                2 -> showDetailDialog("Kuukausimaksut", TransactionRepository.transactions.filter { it.category == Category.SUBSCRIPTION })
                // saldolla ei avata
            }
        }

        // 5) Swipe vain “Jäljellä”-headeriin ja saldoon
        setupSwipe(findViewById(R.id.ll_header))
        setupSwipe(findViewById(R.id.tv_remaining_amount))

        // 6) Pieni “+” tapahtumat-otsikossa
        findViewById<ImageButton>(R.id.btn_add_transaction_small)
            .setOnClickListener { showTransactionDialog() }

        // 7) SUODATUS-NAPIT (“Kaikki” ja “Tulevat”)
        findViewById<TextView>(R.id.tv_filter_all).setOnClickListener {
            currentFilter = Filter.ALL
            rebuildList()
            updateFilterUI()
        }
        findViewById<TextView>(R.id.tv_filter_upcoming).setOnClickListener {
            currentFilter = Filter.UPCOMING
            rebuildList()
            updateFilterUI()
        }

        // 8) Näytetään aluksi koko lista
        rebuildList()
        // 9) Päivitä filter-nappien ulkoasu
        updateFilterUI()
    }

    // 1) Valikon asetukset (☰ oikeassa yläkulmassa)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_menu -> {
                startActivity(Intent(this, ManagePaymentsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    // Päivittää headerin otsikon ja summan sivun (=currentPage) mukaan
    private fun updateRemainingText() {
        // 1) Haetaan kaksi TextView’ta: label (otsikko) ja amount (summa)
        val labelTv  = findViewById<TextView>(R.id.tv_remaining_label)
        val amountTv = findViewById<TextView>(R.id.tv_remaining_amount)

        // 2) Vaihdetaan otsikkoteksti sivun mukaan
        when (currentPage) {
            0 -> labelTv.setText(R.string.remaining)             // “Jäljellä”
            1 -> labelTv.setText(R.string.loans_label)           // “Lainat ja luotot”
            else -> labelTv.setText(R.string.subscriptions_label) // “Kuukausimaksut”
        }

        // 3) Lasketaan, mitkä tapahtumat ovat “tänään tai ennen” (past-list)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);     set(Calendar.MILLISECOND, 0)
        }.time

        val past = TransactionRepository.transactions.mapNotNull { tx ->
            val date = runCatching { fmtIso.parse(tx.time) }.getOrNull()
                ?: runCatching { fmtDot.parse(tx.time) }.getOrNull()
            date?.takeIf { it <= today }?.let { tx }
        }

        // 4) Summataan luvut sivun logiikalla
        val value = when (currentPage) {
            0 -> past.sumOf   { it.amount }                       // Saldo
            1 -> -past.filter { it.amount < 0 }.sumOf { it.amount }   // Lainat (muutetaan positiiviseksi)
            else -> past.filter { it.amount > 0 }.sumOf { it.amount } // Kuukausimaksut
        }

        // 5) Näytetään laskettu summa
        amountTv.text = String.format(Locale.getDefault(), "%.2f €", value)
    }

    // updatePageIndicator: kolme pistettä
    private fun updatePageIndicator() {
        findViewById<ImageView>(R.id.iv_dot1)
            .setImageResource(if (currentPage == 0) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        findViewById<ImageView>(R.id.iv_dot2)
            .setImageResource(if (currentPage == 1) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        findViewById<ImageView>(R.id.iv_dot3)
            .setImageResource(if (currentPage == 2) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        rebuildList()
    }

    private fun updateFilterUI() {
        val tvAll      = findViewById<TextView>(R.id.tv_filter_all)
        val tvUpcoming = findViewById<TextView>(R.id.tv_filter_upcoming)
        when (currentFilter) {
            Filter.ALL -> {
                tvAll.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                tvUpcoming.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
            Filter.UPCOMING -> {
                tvAll.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                tvUpcoming.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }
    // rebuildList: suodattaa “kaikki” vs “tulevat”
    private fun rebuildList() {
        // a) Tänään klo 00:00
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0);    set(Calendar.MILLISECOND,0)
        }.time

        // b) Jaetaan tapahtumat menneisiin/nykyhetkeen ja tuleviin
        val upcoming = TransactionRepository.transactions.filter { tx ->
            val d = runCatching { fmtIso.parse(tx.time) }.getOrNull()
                ?: runCatching { fmtDot.parse(tx.time) }.getOrNull()
            d != null && d.after(today)
        }
        // c) Muut: kaikki paitsi upcoming
        val pastOrToday = TransactionRepository.transactions.filter { !upcoming.contains(it) }

        // d) Valitaan näytettävä data - Aika-suodatus
        val timeFiltered = when (currentFilter) {
            Filter.ALL      -> pastOrToday + upcoming
            Filter.UPCOMING -> upcoming
        }
        // d) Sen jälkeen sivu-suodatus kategoriaan
        val pageFiltered = when (currentPage) {
            0 -> timeFiltered                                        // Saldo = kaikki ajassa
            1 -> timeFiltered.filter { it.category == Category.LOAN }         // Lainat
            2 -> timeFiltered.filter { it.category == Category.SUBSCRIPTION } // Muut (tilaukset)
            else -> timeFiltered
        }

        // e) Päivitetään adapterin data
        adapter.updateItems(pageFiltered)
    }

    // setupSwipe: pyyhkäisy vain headerissa
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe(view: View) {
        var startX = 0f; val threshold = 100
        view.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> startX = ev.x
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    val dx = ev.x - startX
                    currentPage = when {
                        dx >  threshold -> (currentPage + 2) % 3
                        dx < -threshold -> (currentPage + 1) % 3
                        else            -> currentPage
                    }
                    updatePageIndicator()
                    updateRemainingText()
                    rebuildList()
                }
            }
            true
        }
    }

    private fun showDetailDialog(title: String, list: List<Transaction>) {
        val items = list.joinToString("\n") { tx ->
            // Muotoile esim. “Netflix: –12.99 € (2025-06-08)”
            "${tx.label}: ${"%.2f €".format(tx.amount)} (${tx.time})"
        }.ifBlank { "Ei tapahtumia." }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(items)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // showTransactionDialog: lisää uusi tapahtuma
    private fun showTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction, null)
        val etDesc   = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val btnDate  = dialogView.findViewById<Button>(R.id.btn_date_picker)
        val tvDate   = dialogView.findViewById<TextView>(R.id.tv_selected_date)
        val rgType   = dialogView.findViewById<RadioGroup>(R.id.rg_type)

        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                tvDate.text = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", y, m+1, d)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_transaction)
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { dlg, _ ->
                var amt = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                // käännä miinukseksi tarvittaessa
                if (rgType.checkedRadioButtonId != R.id.rb_income) amt = -amt
                // kategoria
                val cat = when (rgType.checkedRadioButtonId) {
                    R.id.rb_income       -> Category.INCOME
                    R.id.rb_expense      -> Category.EXPENSE
                    else                 -> Category.OTHER
                }
                val dateStr = tvDate.text.toString().ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
                TransactionRepository.transactions.add(Transaction(
                    iconRes  = if (amt>=0) R.drawable.ic_bank else R.drawable.ic_shopping,
                    label    = etDesc.text.toString(),
                    amount   = amt,
                    time     = dateStr,
                    category = cat
                ))
                rebuildList()
                updateRemainingText()
                dlg.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
