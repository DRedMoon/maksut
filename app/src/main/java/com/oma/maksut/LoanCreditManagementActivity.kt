package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.widget.PopupMenu
import android.widget.ImageButton
import android.widget.Toast

class LoanCreditManagementActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var llLoanCreditCards: LinearLayout
    private lateinit var tvTotalRemaining: TextView
    private lateinit var tvTotalMonthly: TextView
    private lateinit var tvTotalInterest: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_credit_management)
        
        try {
            repository = FinanceRepository(this)
            setupViews()
            setupToolbar() // Add this missing call
            loadData()
        } catch (e: Exception) {
            android.util.Log.e("LoanCreditManagementActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupViews() {
        llLoanCreditCards = findViewById(R.id.ll_loan_credit_cards)
        tvTotalRemaining = findViewById(R.id.tv_total_remaining)
        tvTotalMonthly = findViewById(R.id.tv_total_monthly)
        tvTotalInterest = findViewById(R.id.tv_total_interest)
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
        popup.menuInflater.inflate(R.menu.menu_loan_credit_management, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_loan_credit -> {
                    startActivity(Intent(this, AddLoanCreditActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            try {
                val loans = repository.getAllActiveLoans().first()
                val credits = repository.getAllActiveCredits().first()
                
                // Clear existing cards
                llLoanCreditCards.removeAllViews()
                
                // Add loan cards first
                loans.forEach { loan ->
                    addLoanCard(loan)
                }
                
                // Add credit cards
                credits.forEach { credit ->
                    addCreditCard(credit)
                }
                
                // Update summary
                updateSummary(loans, credits)
                
            } catch (e: Exception) {
                android.util.Log.e("LoanCreditManagementActivity", "Error loading data", e)
            }
        }
    }
    
    private fun addLoanCard(loan: Loan) {
        val cardView = layoutInflater.inflate(R.layout.item_loan_credit_detailed, llLoanCreditCards, false)
        
        // Set loan data
        cardView.findViewById<TextView>(R.id.tv_loan_credit_name).text = loan.name
        cardView.findViewById<TextView>(R.id.tv_loan_credit_type).text = getString(R.string.loan)
        cardView.findViewById<TextView>(R.id.tv_loan_credit_amount).text = String.format("€%.2f", loan.originalAmount)
        
        // Calculate progress
        val progress = ((loan.originalAmount - loan.remainingBalance) / loan.originalAmount * 100).toInt()
        cardView.findViewById<TextView>(R.id.tv_progress_percentage).text = "$progress%"
        cardView.findViewById<android.widget.ProgressBar>(R.id.pb_repayment_progress).progress = progress
        
        // Set details
        cardView.findViewById<TextView>(R.id.tv_remaining_balance).text = String.format("€%.2f", loan.remainingBalance)
        cardView.findViewById<TextView>(R.id.tv_monthly_payment).text = String.format("€%.2f", loan.monthlyPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_interest_rate).text = String.format("%.1f%%", loan.interestRate)
        cardView.findViewById<TextView>(R.id.tv_total_repayment).text = String.format("€%.2f", loan.totalRepaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_total_interest).text = String.format("€%.2f", loan.totalInterestAmount)
        
        // Set due date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        cardView.findViewById<TextView>(R.id.tv_due_date).text = dateFormat.format(loan.dueDate)
        
        // Set up edit button
        cardView.findViewById<android.widget.Button>(R.id.btn_edit).setOnClickListener {
            // TODO: Open edit loan dialog
        }
        
        // Set up delete button
        cardView.findViewById<android.widget.Button>(R.id.btn_delete).setOnClickListener {
            // TODO: Show delete confirmation
        }
        
        llLoanCreditCards.addView(cardView)
    }
    
    private fun addCreditCard(credit: Credit) {
        val cardView = layoutInflater.inflate(R.layout.item_loan_credit_detailed, llLoanCreditCards, false)
        
        // Set credit data
        cardView.findViewById<TextView>(R.id.tv_loan_credit_name).text = credit.name
        cardView.findViewById<TextView>(R.id.tv_loan_credit_type).text = getString(R.string.credit)
        cardView.findViewById<TextView>(R.id.tv_loan_credit_amount).text = String.format("€%.2f", credit.creditLimit)
        
        // Calculate progress
        val progress = ((credit.creditLimit - credit.currentBalance) / credit.creditLimit * 100).toInt()
        cardView.findViewById<TextView>(R.id.tv_progress_percentage).text = "$progress%"
        cardView.findViewById<android.widget.ProgressBar>(R.id.pb_repayment_progress).progress = progress
        
        // Set details
        cardView.findViewById<TextView>(R.id.tv_remaining_balance).text = String.format("€%.2f", credit.currentBalance)
        cardView.findViewById<TextView>(R.id.tv_monthly_payment).text = String.format("€%.2f", credit.minimumPaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_interest_rate).text = String.format("%.1f%%", credit.totalInterestRate)
        cardView.findViewById<TextView>(R.id.tv_total_repayment).text = String.format("€%.2f", credit.totalRepaymentAmount)
        cardView.findViewById<TextView>(R.id.tv_total_interest).text = String.format("€%.2f", credit.totalInterestAmount)
        
        // Set due date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        cardView.findViewById<TextView>(R.id.tv_due_date).text = dateFormat.format(credit.dueDate)
        
        // Set up edit button
        cardView.findViewById<android.widget.Button>(R.id.btn_edit).setOnClickListener {
            // TODO: Open edit credit dialog
        }
        
        // Set up delete button
        cardView.findViewById<android.widget.Button>(R.id.btn_delete).setOnClickListener {
            // TODO: Show delete confirmation
        }
        
        llLoanCreditCards.addView(cardView)
    }
    
    private fun updateSummary(loans: List<Loan>, credits: List<Credit>) {
        val totalRemaining = loans.sumOf { it.remainingBalance } + credits.sumOf { it.currentBalance }
        val totalMonthly = loans.sumOf { it.monthlyPaymentAmount } + credits.sumOf { it.minimumPaymentAmount }
        val totalInterest = loans.sumOf { it.totalInterestAmount } + credits.sumOf { it.totalInterestAmount }
        
        tvTotalRemaining.text = String.format("€%.2f", totalRemaining)
        tvTotalMonthly.text = String.format("€%.2f", totalMonthly)
        tvTotalInterest.text = String.format("€%.2f", totalInterest)
    }
}