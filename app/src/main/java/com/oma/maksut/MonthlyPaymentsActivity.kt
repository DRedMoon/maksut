package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MonthlyPaymentsActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    
    // Summary TextViews
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvMonthlyPaid: TextView
    private lateinit var tvMonthlyRemaining: TextView
    private lateinit var tvLoansCreditsTotal: TextView
    private lateinit var tvLoansCreditsPaid: TextView
    private lateinit var tvLoansCreditsRemaining: TextView
    
    // Card containers
    private lateinit var llLoansCreditsPayments: LinearLayout
    private lateinit var llMonthlyPayments: LinearLayout
    
    // Show more/less buttons
    private lateinit var btnShowMoreLoansCredits: Button
    private lateinit var btnShowMoreMonthly: Button
    
    // Data
    private var allLoans: List<Loan> = emptyList()
    private var allCredits: List<Credit> = emptyList()
    private var allMonthlyPayments: List<Transaction> = emptyList()
    private var showAllLoansCredits = false
    private var showAllMonthly = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_payments)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupListeners()
        loadData()
    }
    
    private fun setupViews() {
        // Summary TextViews
        tvMonthlyTotal = findViewById(R.id.tv_monthly_total)
        tvMonthlyPaid = findViewById(R.id.tv_monthly_paid)
        tvMonthlyRemaining = findViewById(R.id.tv_monthly_remaining)
        tvLoansCreditsTotal = findViewById(R.id.tv_loans_credits_total)
        tvLoansCreditsPaid = findViewById(R.id.tv_loans_credits_paid)
        tvLoansCreditsRemaining = findViewById(R.id.tv_loans_credits_remaining)
        
        // Card containers
        llLoansCreditsPayments = findViewById(R.id.ll_loans_credits_payments)
        llMonthlyPayments = findViewById(R.id.ll_monthly_payments)
        
        // Show more/less buttons
        btnShowMoreLoansCredits = findViewById(R.id.btn_show_more_loans_credits)
        btnShowMoreMonthly = findViewById(R.id.btn_show_more_monthly)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
        
        // Manual menu button
        findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            showPopupMenu()
        }
    }
    
    private fun showPopupMenu() {
        val menuButton = findViewById<ImageButton>(R.id.btn_menu)
        val popup = PopupMenu(this, menuButton, android.view.Gravity.END)
        popup.menuInflater.inflate(R.menu.menu_monthly_payments, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_show_all_payments -> {
                    startActivity(Intent(this, AllPaymentsActivity::class.java))
                    true
                }
                R.id.action_all_transactions -> {
                    val intent = Intent(this, AllTransactionsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun setupListeners() {
        btnShowMoreLoansCredits.setOnClickListener {
            showAllLoansCredits = !showAllLoansCredits
            updateLoansCreditsDisplay()
        }
        
        btnShowMoreMonthly.setOnClickListener {
            showAllMonthly = !showAllMonthly
            updateMonthlyPaymentsDisplay()
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            try {
                allLoans = repository.getAllActiveLoans().first()
                allCredits = repository.getAllActiveCredits().first()
                allMonthlyPayments = repository.getMonthlyPayments().first()
                
                updateSummary()
                updateLoansCreditsDisplay()
                updateMonthlyPaymentsDisplay()
                
            } catch (e: Exception) {
                android.util.Log.e("MonthlyPaymentsActivity", "Error loading data", e)
            }
        }
    }
    
    private fun updateSummary() {
        // Calculate monthly payments summary
        val monthlyTotal = allMonthlyPayments.sumOf { it.amount }
        val monthlyPaid = allMonthlyPayments.filter { it.isPaid }.sumOf { it.amount }
        val monthlyRemaining = monthlyTotal - monthlyPaid
        
        tvMonthlyTotal.text = String.format("€%.2f", monthlyTotal)
        tvMonthlyPaid.text = String.format("€%.2f", monthlyPaid)
        tvMonthlyRemaining.text = String.format("€%.2f", monthlyRemaining)
        
        // Calculate loans & credits summary
        val loansCreditsTotal = allLoans.sumOf { it.monthlyPaymentAmount } + allCredits.sumOf { it.minimumPaymentAmount }
        val loansCreditsPaid = allLoans.filter { it.isPaid }.sumOf { it.monthlyPaymentAmount } + 
                              allCredits.filter { it.isPaid }.sumOf { it.minimumPaymentAmount }
        val loansCreditsRemaining = loansCreditsTotal - loansCreditsPaid
        
        tvLoansCreditsTotal.text = String.format("€%.2f", loansCreditsTotal)
        tvLoansCreditsPaid.text = String.format("€%.2f", loansCreditsPaid)
        tvLoansCreditsRemaining.text = String.format("€%.2f", loansCreditsRemaining)
    }
    
    private fun updateLoansCreditsDisplay() {
        llLoansCreditsPayments.removeAllViews()
        
        val allPayments = mutableListOf<Any>()
        allPayments.addAll(allLoans)
        allPayments.addAll(allCredits)
        
        val displayCount = if (showAllLoansCredits) allPayments.size else minOf(2, allPayments.size)
        
        for (i in 0 until displayCount) {
            val payment = allPayments[i]
            when (payment) {
                is Loan -> addLoanPaymentCard(payment)
                is Credit -> addCreditPaymentCard(payment)
            }
        }
        
        // Show/hide show more button
        btnShowMoreLoansCredits.visibility = if (allPayments.size > 2) View.VISIBLE else View.GONE
        btnShowMoreLoansCredits.text = if (showAllLoansCredits) getString(R.string.show_less) else getString(R.string.show_more)
    }
    
    private fun updateMonthlyPaymentsDisplay() {
        llMonthlyPayments.removeAllViews()
        
        val displayCount = if (showAllMonthly) allMonthlyPayments.size else minOf(3, allMonthlyPayments.size)
        
        for (i in 0 until displayCount) {
            addMonthlyPaymentCard(allMonthlyPayments[i])
        }
        
        // Show/hide show more button
        btnShowMoreMonthly.visibility = if (allMonthlyPayments.size > 3) View.VISIBLE else View.GONE
        btnShowMoreMonthly.text = if (showAllMonthly) getString(R.string.show_less) else getString(R.string.show_more)
    }
    
    private fun addLoanPaymentCard(loan: Loan) {
        val cardView = layoutInflater.inflate(R.layout.item_payment_card, llLoansCreditsPayments, false)
        
        cardView.findViewById<TextView>(R.id.tv_payment_name).text = loan.name
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", loan.monthlyPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = getString(R.string.loan)
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        cardView.findViewById<TextView>(R.id.tv_due_date).text = "Due: ${dateFormat.format(loan.dueDate)}"
        
        val checkBox = cardView.findViewById<CheckBox>(R.id.cb_payment_paid)
        checkBox.isChecked = loan.isPaid
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                repository.updateLoanPaymentStatus(loan.id, isChecked)
                updateSummary()
            }
        }
        
        llLoansCreditsPayments.addView(cardView)
    }
    
    private fun addCreditPaymentCard(credit: Credit) {
        val cardView = layoutInflater.inflate(R.layout.item_payment_card, llLoansCreditsPayments, false)
        
        cardView.findViewById<TextView>(R.id.tv_payment_name).text = credit.name
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", credit.minimumPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = getString(R.string.credit)
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        cardView.findViewById<TextView>(R.id.tv_due_date).text = "Due: ${dateFormat.format(credit.dueDate)}"
        
        val checkBox = cardView.findViewById<CheckBox>(R.id.cb_payment_paid)
        checkBox.isChecked = credit.isPaid
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                repository.updateCreditPaymentStatus(credit.id, isChecked)
                updateSummary()
            }
        }
        
        llLoansCreditsPayments.addView(cardView)
    }
    
    private fun addMonthlyPaymentCard(transaction: Transaction) {
        val cardView = layoutInflater.inflate(R.layout.item_payment_card, llMonthlyPayments, false)
        
        cardView.findViewById<TextView>(R.id.tv_payment_name).text = transaction.name
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", transaction.amount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = "Monthly"
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        cardView.findViewById<TextView>(R.id.tv_due_date).text = "Due: ${dateFormat.format(transaction.paymentDate)}"
        
        val checkBox = cardView.findViewById<CheckBox>(R.id.cb_payment_paid)
        checkBox.isChecked = transaction.isPaid
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                repository.updatePaymentStatus(transaction.id, isChecked)
                updateSummary()
            }
        }
        
        llMonthlyPayments.addView(cardView)
    }
}