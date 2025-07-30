package com.oma.maksut

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class AllPaymentsActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    
    // Summary TextViews
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvMonthlyPaid: TextView
    private lateinit var tvMonthlyUnpaid: TextView
    private lateinit var tvLoansCreditsTotal: TextView
    private lateinit var tvLoansCreditsPaid: TextView
    private lateinit var tvLoansCreditsUnpaid: TextView
    
    // Card containers
    private lateinit var llLoansCreditsPayments: LinearLayout
    private lateinit var llMonthlyPayments: LinearLayout
    
    // Show more/less buttons
    private lateinit var llLoansCreditsButtons: LinearLayout
    private lateinit var btnShowMoreLoansCredits: Button
    private lateinit var btnShowLessLoansCredits: Button
    private lateinit var llMonthlyPaymentsButtons: LinearLayout
    private lateinit var btnShowMoreMonthlyPayments: Button
    private lateinit var btnShowLessMonthlyPayments: Button
    
    private var allLoans = listOf<Loan>()
    private var allCredits = listOf<Credit>()
    private var allMonthlyPayments = listOf<Transaction>()
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_payments)
        
        try {
            repository = FinanceRepository(this)
            setupViews()
            setupToolbar()
            loadData()
        } catch (e: Exception) {
            android.util.Log.e("AllPaymentsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupViews() {
        // Summary TextViews
        tvMonthlyTotal = findViewById(R.id.tv_monthly_total)
        tvMonthlyPaid = findViewById(R.id.tv_monthly_paid)
        tvMonthlyUnpaid = findViewById(R.id.tv_monthly_unpaid)
        tvLoansCreditsTotal = findViewById(R.id.tv_loans_credits_total)
        tvLoansCreditsPaid = findViewById(R.id.tv_loans_credits_paid)
        tvLoansCreditsUnpaid = findViewById(R.id.tv_loans_credits_unpaid)
        
        // Card containers
        llLoansCreditsPayments = findViewById(R.id.ll_loans_credits_payments)
        llMonthlyPayments = findViewById(R.id.ll_monthly_payments)
        
        // Show more/less buttons
        llLoansCreditsButtons = findViewById(R.id.ll_loans_credits_buttons)
        btnShowMoreLoansCredits = findViewById(R.id.btn_show_more_loans_credits)
        btnShowLessLoansCredits = findViewById(R.id.btn_show_less_loans_credits)
        llMonthlyPaymentsButtons = findViewById(R.id.ll_monthly_payments_buttons)
        btnShowMoreMonthlyPayments = findViewById(R.id.btn_show_more_monthly_payments)
        btnShowLessMonthlyPayments = findViewById(R.id.btn_show_less_monthly_payments)
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
        popup.menuInflater.inflate(R.menu.menu_all_payments, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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
        // Show more/less for loans and credits
        btnShowMoreLoansCredits.setOnClickListener {
            updateLoansCreditsDisplay(allLoans, allCredits, true)
            llLoansCreditsButtons.visibility = View.VISIBLE
            btnShowMoreLoansCredits.visibility = View.GONE
            btnShowLessLoansCredits.visibility = View.VISIBLE
        }
        
        btnShowLessLoansCredits.setOnClickListener {
            updateLoansCreditsDisplay(allLoans, allCredits, false)
            llLoansCreditsButtons.visibility = View.VISIBLE
            btnShowMoreLoansCredits.visibility = View.VISIBLE
            btnShowLessLoansCredits.visibility = View.GONE
        }
        
        // Show more/less for monthly payments
        btnShowMoreMonthlyPayments.setOnClickListener {
            updateMonthlyPaymentsDisplay(allMonthlyPayments, true)
            llMonthlyPaymentsButtons.visibility = View.VISIBLE
            btnShowMoreMonthlyPayments.visibility = View.GONE
            btnShowLessMonthlyPayments.visibility = View.VISIBLE
        }
        
        btnShowLessMonthlyPayments.setOnClickListener {
            updateMonthlyPaymentsDisplay(allMonthlyPayments, false)
            llMonthlyPaymentsButtons.visibility = View.VISIBLE
            btnShowMoreMonthlyPayments.visibility = View.VISIBLE
            btnShowLessMonthlyPayments.visibility = View.GONE
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            try {
                allLoans = repository.getAllActiveLoans().first()
                allCredits = repository.getAllActiveCredits().first()
                allMonthlyPayments = repository.getMonthlyPayments().first()
                
                updateSummary()
                updateLoansCreditsDisplay(allLoans, allCredits, false)
                updateMonthlyPaymentsDisplay(allMonthlyPayments, false)
            } catch (e: Exception) {
                android.util.Log.e("AllPaymentsActivity", "Error loading data", e)
            }
        }
    }
    
    private fun updateSummary() {
        // Monthly payments summary
        val monthlyTotal = allMonthlyPayments.sumOf { it.amount }
        val monthlyPaid = allMonthlyPayments.filter { it.isPaid }.sumOf { it.amount }
        val monthlyUnpaid = monthlyTotal - monthlyPaid
        
        tvMonthlyTotal.text = String.format("€%.2f", monthlyTotal)
        tvMonthlyPaid.text = String.format("€%.2f", monthlyPaid)
        tvMonthlyUnpaid.text = String.format("€%.2f", monthlyUnpaid)
        
        // Loans and credits summary
        val loansCreditsTotal = allLoans.sumOf { it.monthlyPaymentAmount } + allCredits.sumOf { it.minimumPaymentAmount }
        val loansCreditsPaid = allLoans.filter { it.isPaid }.sumOf { it.monthlyPaymentAmount } + 
                              allCredits.filter { it.isPaid }.sumOf { it.minimumPaymentAmount }
        val loansCreditsUnpaid = loansCreditsTotal - loansCreditsPaid
        
        tvLoansCreditsTotal.text = String.format("€%.2f", loansCreditsTotal)
        tvLoansCreditsPaid.text = String.format("€%.2f", loansCreditsPaid)
        tvLoansCreditsUnpaid.text = String.format("€%.2f", loansCreditsUnpaid)
    }
    
    private fun updateLoansCreditsDisplay(loans: List<Loan>, credits: List<Credit>, showAll: Boolean) {
        llLoansCreditsPayments.removeAllViews()
        
        val itemsToShow = if (showAll) {
            loans + credits
        } else {
            (loans + credits).take(3)
        }
        
        itemsToShow.forEach { item ->
            when (item) {
                is Loan -> addLoanPaymentCard(item)
                is Credit -> addCreditPaymentCard(item)
            }
        }
        
        // Show/hide buttons
        if (loans.size + credits.size > 3) {
            llLoansCreditsButtons.visibility = View.VISIBLE
            btnShowMoreLoansCredits.visibility = if (showAll) View.GONE else View.VISIBLE
            btnShowLessLoansCredits.visibility = if (showAll) View.VISIBLE else View.GONE
        } else {
            llLoansCreditsButtons.visibility = View.GONE
        }
    }
    
    private fun updateMonthlyPaymentsDisplay(transactions: List<Transaction>, showAll: Boolean) {
        llMonthlyPayments.removeAllViews()
        
        val itemsToShow = if (showAll) {
            transactions
        } else {
            transactions.take(3)
        }
        
        itemsToShow.forEach { transaction ->
            addMonthlyPaymentCard(transaction)
        }
        
        // Show/hide buttons
        if (transactions.size > 3) {
            llMonthlyPaymentsButtons.visibility = View.VISIBLE
            btnShowMoreMonthlyPayments.visibility = if (showAll) View.GONE else View.VISIBLE
            btnShowLessMonthlyPayments.visibility = if (showAll) View.VISIBLE else View.GONE
        } else {
            llMonthlyPaymentsButtons.visibility = View.GONE
        }
    }
    
    private fun addLoanPaymentCard(loan: Loan) {
        val cardView = layoutInflater.inflate(R.layout.item_payment_card, llLoansCreditsPayments, false)
        
        cardView.findViewById<TextView>(R.id.tv_payment_name).text = loan.name
        cardView.findViewById<TextView>(R.id.tv_due_date).text = "Day ${loan.dueDay}"
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", loan.monthlyPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = getString(R.string.loan)
        
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
        cardView.findViewById<TextView>(R.id.tv_due_date).text = "Day ${credit.dueDay}"
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", credit.minimumPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = getString(R.string.credit)
        
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
        transaction.dueDate?.let { dueDate ->
            cardView.findViewById<TextView>(R.id.tv_due_date).text = dateFormat.format(dueDate)
        } ?: run {
            cardView.findViewById<TextView>(R.id.tv_due_date).text = "No due date"
        }
        cardView.findViewById<TextView>(R.id.tv_payment_amount).text = String.format("€%.2f", transaction.amount)
        cardView.findViewById<TextView>(R.id.tv_payment_type).text = "Monthly Payment"
        
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