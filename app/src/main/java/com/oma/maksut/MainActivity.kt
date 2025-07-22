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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import com.oma.maksut.database.entities.Transaction

// 0) Luokan kentät: suodatus-enum, formaatit, nykyinen suodatin
class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TransactionAdapter
    private lateinit var repository: FinanceRepository

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
        
        repository = FinanceRepository(this)

        // 1) Toolbar (Maksut + rattaan/valikon ikonit)
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        // 2) RecyclerView (Tapahtumat)
        adapter = TransactionAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rv_transactions).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // 3) Load real transactions from database
        lifecycleScope.launch {
            repository.getRealTransactions().collect { transactions ->
                adapter.updateItems(transactions)
                val value = transactions.sumOf { it.amount }
                findViewById<TextView>(R.id.tv_remaining_amount).text = String.format(Locale.getDefault(), "%.2f €", value)
            }
        }

        // 4) Alku-näkymä: saldon ja indikaattorin päivitys
        updateRemainingText()
        updatePageIndicator()

        // Single click and double click handling for loan/credit amounts
        val tvRemainingAmount = findViewById<TextView>(R.id.tv_remaining_amount)
        var clickCount = 0
        var lastClickTime = 0L
        
        tvRemainingAmount.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 300) {
                // Double click detected
                clickCount = 0
                when (currentPage) {
                    1 -> showLoanCreditDetailsDialog()
                    2 -> startActivity(Intent(this, MonthlyPaymentsActivity::class.java))
                    // saldolla ei avata
                }
            } else {
                // Single click
                clickCount++
                when (currentPage) {
                    1 -> startActivity(Intent(this, LoanCreditManagementActivity::class.java))
                    2 -> startActivity(Intent(this, MonthlyPaymentsActivity::class.java))
                    // saldolla ei avata
                }
            }
            lastClickTime = currentTime
        }

        // 5) Swipe vain “Jäljellä”-headeriin ja saldoon
        setupSwipe(findViewById(R.id.ll_header))
        setupSwipe(findViewById(R.id.tv_remaining_amount))

        // 6) Pieni “+” tapahtumat-otsikossa

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

        // 7a) Transactions header 3-dot menu
        findViewById<ImageView>(R.id.iv_transactions_menu).setOnClickListener {
            startActivity(Intent(this, AllPaymentsActivity::class.java))
        }

        // 8) Näytetään aluksi koko lista
        rebuildList()
        // 9) Päivitä filter-nappien ulkoasu
        updateFilterUI()
        
        // 10) Bottom Navigation Setup
        setupBottomNavigation()

        // Initialize default categories and load data from repository
        lifecycleScope.launch {
            repository.initializeDefaultCategories()
            repository.getRealTransactions().collect { transactions ->
                adapter.updateItems(transactions)
                // Update balance
                val value = transactions.sumOf { it.amount }
                findViewById<TextView>(R.id.tv_remaining_amount).text = String.format(Locale.getDefault(), "%.2f €", value)
            }
        }
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

        lifecycleScope.launch {
            val transactions = repository.getRealTransactions().first()
            val past = transactions.filter { tx ->
                tx.paymentDate <= today
            }

            // 4) Summataan luvut sivun logiikalla
            val value = when (currentPage) {
                0 -> past.sumOf { it.amount }                       // Saldo
                1 -> -past.filter { it.amount < 0 }.sumOf { it.amount }   // Lainat (muutetaan positiiviseksi)
                else -> past.filter { it.amount > 0 }.sumOf { it.amount } // Kuukausimaksut
            }

            // 5) Näytetään laskettu summa
            amountTv.text = String.format(Locale.getDefault(), "%.2f €", value)
        }
    }

    // updatePageIndicator: kolme pistettä
    private fun updatePageIndicator() {
        findViewById<ImageView>(R.id.iv_dot1)
            .setImageResource(if (currentPage == 0) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        findViewById<ImageView>(R.id.iv_dot2)
            .setImageResource(if (currentPage == 1) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        findViewById<ImageView>(R.id.iv_dot3)
            .setImageResource(if (currentPage == 2) R.drawable.ic_dot_filled else R.drawable.ic_dot_outline)
        
        // Update page title and content
        updateRemainingText()
        rebuildList()
        // Don't automatically open MonthlyPaymentsActivity - let user click on amount instead
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

        lifecycleScope.launch {
            val transactions = repository.getRealTransactions().first()
            // b) Jaetaan tapahtumat menneisiin/nykyhetkeen ja tuleviin
            val upcoming = transactions.filter { tx ->
                tx.paymentDate.after(today)
            }
            // c) Muut: kaikki paitsi upcoming
            val pastOrToday = transactions.filter { !upcoming.contains(it) }

            // d) Valitaan näytettävä data - Aika-suodatus
            val timeFiltered = when (currentFilter) {
                Filter.ALL      -> pastOrToday + upcoming
                Filter.UPCOMING -> upcoming
            }
            // d) Sen jälkeen sivu-suodatus kategoriaan
            val pageFiltered = when (currentPage) {
                0 -> timeFiltered                                        // Saldo = kaikki ajassa
                1 -> timeFiltered.filter { it.isLoanRepayment }         // Lainat
                2 -> timeFiltered.filter { it.isMonthlyPayment } // Muut (tilaukset)
                else -> timeFiltered
            }

            // e) Päivitetään adapterin data
            adapter.updateItems(pageFiltered)
        }
    }

    // setupSwipe: pyyhkäisy vain headerissa
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe(view: View) {
        var startX = 0f; val threshold = 80 // Reduced threshold for better responsiveness
        view.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> startX = ev.x
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    val dx = ev.x - startX
                    if (Math.abs(dx) > threshold) {
                        currentPage = when {
                            dx > 0 -> (currentPage + 2) % 3  // Swipe right -> previous page
                            else   -> (currentPage + 1) % 3  // Swipe left -> next page
                        }
                        updatePageIndicator()
                    }
                }
            }
            true
        }
    }

    private fun showDetailDialog(title: String, list: List<Transaction>) {
        val items = list.joinToString("\n") { tx ->
            // Muotoile esim. “Netflix: –12.99 € (2025-06-08)”
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            "${tx.name}: ${"%.2f €".format(tx.amount)} (${fmt.format(tx.paymentDate)})"
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
                // kategoria ID (default to 1 for now)
                val categoryId = when (rgType.checkedRadioButtonId) {
                    R.id.rb_income       -> 1L
                    R.id.rb_expense      -> 2L
                    else                 -> 3L
                }
                val dateStr = tvDate.text.toString().ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
                lifecycleScope.launch {
                    val transaction = Transaction(
                        name = etDesc.text.toString(),
                        amount = amt,
                        categoryId = categoryId,
                        paymentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date(),
                        description = etDesc.text.toString()
                    )
                    repository.insertTransaction(transaction)
                    rebuildList()
                    updateRemainingText()
                }
                dlg.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    // Bottom Navigation Setup
    private fun setupBottomNavigation() {
        // Home button - already on home
        findViewById<LinearLayout>(R.id.btn_home).setOnClickListener {
            // Already on home page, just refresh
            currentPage = 0
            updatePageIndicator()
            updateRemainingText()
            updateBottomNavSelection(R.id.btn_home)
        }
        
        // Upcoming button - open upcoming activity
        findViewById<LinearLayout>(R.id.btn_upcoming).setOnClickListener {
            startActivity(Intent(this, UpcomingActivity::class.java))
            updateBottomNavSelection(R.id.btn_upcoming)
        }
        
        // Add button - show quick add transaction activity
        findViewById<LinearLayout>(R.id.btn_add).setOnClickListener {
            startActivity(Intent(this, QuickAddTransactionActivity::class.java))
            updateBottomNavSelection(R.id.btn_add)
        }
        
        // Analysis button - open analysis activity
        findViewById<LinearLayout>(R.id.btn_analysis).setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            updateBottomNavSelection(R.id.btn_analysis)
        }
        
        // Settings button - open settings
        findViewById<LinearLayout>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            updateBottomNavSelection(R.id.btn_settings)
        }
        
        // Set initial selection
        updateBottomNavSelection(R.id.btn_home)
    }
    
    private fun updateBottomNavSelection(selectedId: Int) {
        val buttons = listOf(
            R.id.btn_home,
            R.id.btn_upcoming,
            R.id.btn_add,
            R.id.btn_analysis,
            R.id.btn_settings
        )
        
        buttons.forEach { buttonId ->
            val button = findViewById<LinearLayout>(buttonId)
            if (buttonId == selectedId) {
                button.setBackgroundResource(R.drawable.bottom_nav_selected)
            } else {
                button.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
    
    private fun showLoanCreditDetailsDialog() {
        lifecycleScope.launch {
            val loans = repository.getAllActiveLoans().first()
            val credits = repository.getAllActiveCredits().first()
            
            val allItems = mutableListOf<LoanCreditItem>()
            
            // Add loans
            loans.forEach { loan ->
                allItems.add(LoanCreditItem(
                    name = loan.name,
                    amount = loan.originalAmount,
                    interestRate = loan.totalInterestRate,
                    totalInterest = loan.totalRepaymentAmount - loan.originalAmount,
                    totalAmount = loan.totalRepaymentAmount,
                    dueDate = loan.endDate,
                    isLoan = true
                ))
            }
            
            // Add credits
            credits.forEach { credit ->
                allItems.add(LoanCreditItem(
                    name = credit.name,
                    amount = credit.creditLimit,
                    interestRate = credit.totalInterestRate,
                    totalInterest = credit.creditLimit * (credit.totalInterestRate / 100),
                    totalAmount = credit.creditLimit + (credit.creditLimit * (credit.totalInterestRate / 100)),
                    dueDate = null, // Credits don't have end dates
                    isLoan = false
                ))
            }
            
            // Sort by total amount (highest first)
            allItems.sortByDescending { it.totalAmount }
            
            // Show dialog with first item and "Show More" option
            showLoanCreditListDialog(allItems, 0)
        }
    }
    
    private fun showLoanCreditListDialog(items: List<LoanCreditItem>, startIndex: Int) {
        val endIndex = minOf(startIndex + 1, items.size)
        val currentItems = items.subList(startIndex, endIndex)
        
        val message = currentItems.joinToString("\n\n") { item ->
            buildString {
                append("${if (item.isLoan) "Laina" else "Luotto"}: ${item.name}\n")
                append("Määrä: ${String.format(Locale.getDefault(), "%.2f €", item.amount)}\n")
                append("Korko: ${String.format(Locale.getDefault(), "%.2f", item.interestRate)}%\n")
                append("Kokonaiskorko: ${String.format(Locale.getDefault(), "%.2f €", item.totalInterest)}\n")
                append("Kokonaissumma: ${String.format(Locale.getDefault(), "%.2f €", item.totalAmount)}")
                if (item.dueDate != null) {
                    append("\nEräpäivä: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(item.dueDate)}")
                }
            }
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Lainat ja luotot")
            .setMessage(message)
            .setPositiveButton("OK", null)
        
        // Add "Show More" button if there are more items
        if (endIndex < items.size) {
            dialog.setNeutralButton("Näytä lisää") { _, _ ->
                showLoanCreditListDialog(items, endIndex)
            }
        }
        
        dialog.show()
    }
    
    data class LoanCreditItem(
        val name: String,
        val amount: Double,
        val interestRate: Double,
        val totalInterest: Double,
        val totalAmount: Double,
        val dueDate: Date?,
        val isLoan: Boolean
    )
}
