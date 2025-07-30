package com.oma.maksut

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.adapters.LoanCreditExpandedAdapter
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import android.view.WindowManager

// Data class for loan/credit items in the expanded view
data class LoanCreditItem(
    val id: Long,
    val name: String,
    val amount: Double,
    val interestRate: Double,
    val totalInterest: Double,
    val totalAmount: Double,
    val dueDate: String,
    val type: String
)

class MainActivity : AppCompatActivity() {
    private lateinit var repository: FinanceRepository
    private lateinit var adapter: TransactionAdapter
    private var currentPage = 0
    private var currentFilter = Filter.ALL

    enum class Filter {
        ALL, UPCOMING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        try {
            repository = FinanceRepository(this)
            
            setupViews()
            setupSwipe()
            setupFilterButtons()
            setupBottomNavigation()
            setupCardClickListeners()
            loadTransactions()
            updateCardContent()
            updatePageIndicator()
            setupSystemBars()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun isPinProtectionEnabled(): Boolean {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val pinEnabled = prefs.getBoolean("pin_code_enabled", false)
        val pinCode = prefs.getString("pin_code", "") ?: ""
        return pinEnabled && pinCode.isNotEmpty()
    }

    private fun setupViews() {
        adapter = TransactionAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rv_transactions).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe() {
        val cardsContainer = findViewById<View>(R.id.fl_cards_container)
        var startX = 0f
        var startY = 0f
        var isSwiping = false
        val threshold = 100f // Reduced threshold for easier swiping
        
        cardsContainer.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    isSwiping = false
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - startX
                    val deltaY = event.y - startY
                    
                    // Check if this is a horizontal swipe
                    if (Math.abs(deltaX) > 20 && Math.abs(deltaX) > Math.abs(deltaY) * 1.5) {
                        isSwiping = true
                    }
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    if (isSwiping) {
                        val deltaX = event.x - startX
                        if (Math.abs(deltaX) > threshold) {
                            if (deltaX > 0) {
                                // Swipe right - go to previous page
                                if (currentPage > 0) {
                                    currentPage--
                                    updateCardContent()
                                    updatePageIndicator()
                                }
                            } else {
                                // Swipe left - go to next page
                                if (currentPage < 2) {
                                    currentPage++
                                    updateCardContent()
                                    updatePageIndicator()
                                }
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.btn_home).setOnClickListener {
            currentPage = 0
            updateCardContent()
            updatePageIndicator()
        }

        findViewById<LinearLayout>(R.id.btn_upcoming).setOnClickListener {
            try {
                startActivity(Intent(this, UpcomingActivity::class.java))
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error opening UpcomingActivity", e)
                Toast.makeText(this, "Error opening upcoming", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<LinearLayout>(R.id.btn_add).setOnClickListener {
            startActivity(Intent(this, QuickAddTransactionActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btn_analysis).setOnClickListener {
            try {
                startActivity(Intent(this, AnalysisActivity::class.java))
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error opening AnalysisActivity", e)
                Toast.makeText(this, "Error opening analysis", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<LinearLayout>(R.id.btn_settings).setOnClickListener {
            try {
                android.util.Log.d("MainActivity", "Settings button clicked")
                val intent = Intent(this, SettingsActivity::class.java)
                android.util.Log.d("MainActivity", "Intent created: $intent")
                startActivity(intent)
                android.util.Log.d("MainActivity", "SettingsActivity started")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error opening SettingsActivity", e)
                Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCardClickListeners() {
        // Balance card click listeners
        findViewById<TextView>(R.id.tv_balance_amount).setOnClickListener {
            // No action needed for balance
        }

        // Loans & Credits card click listeners
        findViewById<TextView>(R.id.tv_total_debt).setOnClickListener {
            toggleLoansCreditsList()
        }

        findViewById<TextView>(R.id.tv_monthly_loan_payments).setOnClickListener {
            toggleLoansCreditsList()
        }

        findViewById<Button>(R.id.btn_manage_loans).setOnClickListener {
            startActivity(Intent(this, LoanCreditManagementActivity::class.java))
        }

        // Monthly Payments card click listeners
        findViewById<TextView>(R.id.tv_monthly_total).setOnClickListener {
            toggleMonthlyPaymentsList()
        }

        findViewById<TextView>(R.id.tv_monthly_paid).setOnClickListener {
            toggleMonthlyPaymentsList()
        }

        findViewById<Button>(R.id.btn_show_all_payments).setOnClickListener {
            startActivity(Intent(this, AllPaymentsActivity::class.java))
        }

        // Transaction arrow button
        findViewById<ImageView>(R.id.btn_view_all_transactions).setOnClickListener {
            startActivity(Intent(this, AllTransactionsActivity::class.java))
        }
    }

    private fun setupFilterButtons() {
        findViewById<Button>(R.id.tv_filter_all).setOnClickListener {
            currentFilter = Filter.ALL
            updateFilterUI()
            loadTransactions()
        }
        
        findViewById<Button>(R.id.tv_filter_upcoming).setOnClickListener {
            currentFilter = Filter.UPCOMING
            updateFilterUI()
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                val allTransactions = repository.getRealTransactions().first()
                val filteredTransactions = when (currentFilter) {
                    Filter.ALL -> allTransactions
                    Filter.UPCOMING -> allTransactions.filter { 
                        it.dueDate != null && it.dueDate.after(Date()) 
                    }.sortedBy { it.dueDate }
                }
                adapter.updateItems(filteredTransactions)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading transactions", e)
            }
        }
    }

    private fun updateFilterUI() {
        val allButton = findViewById<Button>(R.id.tv_filter_all)
        val upcomingButton = findViewById<Button>(R.id.tv_filter_upcoming)
        
        when (currentFilter) {
            Filter.ALL -> {
                allButton.setBackgroundResource(R.drawable.button_background)
                upcomingButton.setBackgroundResource(R.drawable.button_background)
            }
            Filter.UPCOMING -> {
                allButton.setBackgroundResource(R.drawable.button_background)
                upcomingButton.setBackgroundResource(R.drawable.button_background)
            }
        }
    }

    private fun updateCardContent() {
        when (currentPage) {
            0 -> showBalanceCard()
            1 -> showLoansCreditsCard()
            2 -> showMonthlyPaymentsCard()
        }
    }

    private fun toggleLoansCreditsList() {
        val loansCreditsList = findViewById<RecyclerView>(R.id.rv_loans_credits)
        val btnShowMore = findViewById<Button>(R.id.btn_show_more_loans)
        val btnShowLess = findViewById<Button>(R.id.btn_show_less_loans)
        
        if (loansCreditsList.visibility == View.VISIBLE) {
            loansCreditsList.visibility = View.GONE
            btnShowMore.visibility = View.VISIBLE
            btnShowLess.visibility = View.GONE
        } else {
            loansCreditsList.visibility = View.VISIBLE
            loadLoansCredits()
            btnShowMore.visibility = View.GONE
            btnShowLess.visibility = View.VISIBLE
        }
    }

    private fun toggleMonthlyPaymentsList() {
        val monthlyPaymentsList = findViewById<RecyclerView>(R.id.rv_monthly_payments)
        val btnShowMore = findViewById<Button>(R.id.btn_show_more_monthly)
        val btnShowLess = findViewById<Button>(R.id.btn_show_less_monthly)
        
        if (monthlyPaymentsList.visibility == View.VISIBLE) {
            monthlyPaymentsList.visibility = View.GONE
            btnShowMore.visibility = View.VISIBLE
            btnShowLess.visibility = View.GONE
        } else {
            monthlyPaymentsList.visibility = View.VISIBLE
            loadMonthlyPayments()
            btnShowMore.visibility = View.GONE
            btnShowLess.visibility = View.VISIBLE
        }
    }

    private fun showBalanceCard() {
        findViewById<View>(R.id.card_balance).visibility = View.VISIBLE
        findViewById<View>(R.id.card_loans_credits).visibility = View.GONE
        findViewById<View>(R.id.card_monthly_payments).visibility = View.GONE
        
        // Show balance content (filter buttons and transactions)
        findViewById<View>(R.id.ll_balance_content).visibility = View.VISIBLE
        
        updateBalanceAmount()
    }

    private fun showLoansCreditsCard() {
        findViewById<View>(R.id.card_balance).visibility = View.GONE
        findViewById<View>(R.id.card_loans_credits).visibility = View.VISIBLE
        findViewById<View>(R.id.card_monthly_payments).visibility = View.GONE
        
        // Hide balance content
        findViewById<View>(R.id.ll_balance_content).visibility = View.GONE
        
        updateLoansCreditsAmount()
    }

    private fun showMonthlyPaymentsCard() {
        findViewById<View>(R.id.card_balance).visibility = View.GONE
        findViewById<View>(R.id.card_loans_credits).visibility = View.GONE
        findViewById<View>(R.id.card_monthly_payments).visibility = View.VISIBLE
        
        // Hide balance content
        findViewById<View>(R.id.ll_balance_content).visibility = View.GONE
        
        updateMonthlyPaymentsAmount()
    }

    private fun updateBalanceAmount() {
        lifecycleScope.launch {
            try {
                val balance = repository.getBalance()
                findViewById<TextView>(R.id.tv_balance_amount).text = String.format("€%.2f", balance)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error updating balance", e)
            }
        }
    }

    private fun updateLoansCreditsAmount() {
        lifecycleScope.launch {
            try {
                val loans = repository.getAllActiveLoans().first()
                val credits = repository.getAllActiveCredits().first()
                val totalDebt = loans.sumOf { it.remainingBalance } + credits.sumOf { it.currentBalance }
                val totalMonthly = loans.sumOf { it.monthlyPaymentAmount } + credits.sumOf { it.minimumPaymentAmount }
                
                findViewById<TextView>(R.id.tv_total_debt).text = String.format("€%.2f", totalDebt)
                findViewById<TextView>(R.id.tv_monthly_loan_payments).text = String.format("€%.2f", totalMonthly)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error updating loans & credits", e)
            }
        }
    }

    private fun updateMonthlyPaymentsAmount() {
        lifecycleScope.launch {
            try {
                val monthlyPayments = repository.getMonthlyPayments().first()
                val total = monthlyPayments.sumOf { it.amount }
                val paid = monthlyPayments.filter { it.isPaid }.sumOf { it.amount }
                val remaining = total - paid
                
                findViewById<TextView>(R.id.tv_monthly_total).text = String.format("€%.2f", total)
                findViewById<TextView>(R.id.tv_monthly_paid).text = String.format("€%.2f", paid)
                findViewById<TextView>(R.id.tv_monthly_remaining).text = String.format("€%.2f", remaining)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error updating monthly payments", e)
            }
        }
    }

    private fun loadLoansCredits() {
        lifecycleScope.launch {
            try {
                val loans = repository.getAllActiveLoans().first()
                val credits = repository.getAllActiveCredits().first()
                
                val items = mutableListOf<LoanCreditItem>()
                
                // Add loans
                loans.forEach { loan ->
                    val totalInterest = (loan.monthlyPaymentAmount * loan.loanTermYears * 12) - loan.originalAmount
                    items.add(LoanCreditItem(
                        id = loan.id,
                        name = loan.name,
                        amount = loan.originalAmount,
                        interestRate = loan.interestRate,
                        totalInterest = totalInterest,
                        totalAmount = loan.totalRepaymentAmount,
                        dueDate = "Day ${loan.dueDay}",
                        type = "Loan"
                    ))
                }
                
                // Add credits
                credits.forEach { credit ->
                    val totalInterest = credit.currentBalance * (credit.interestRate / 100) * 12
                    items.add(LoanCreditItem(
                        id = credit.id,
                        name = credit.name,
                        amount = credit.creditLimit,
                        interestRate = credit.interestRate,
                        totalInterest = totalInterest,
                        totalAmount = credit.currentBalance,
                        dueDate = "Day ${credit.dueDay}",
                        type = "Credit"
                    ))
                }
                
                val adapter = LoanCreditExpandedAdapter(items)
                findViewById<RecyclerView>(R.id.rv_loans_credits).apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    this.adapter = adapter
                }
                
                // Show/hide show more/less buttons
                val btnShowMore = findViewById<Button>(R.id.btn_show_more_loans)
                val btnShowLess = findViewById<Button>(R.id.btn_show_less_loans)
                
                if (items.size > 3) {
                    btnShowMore.visibility = View.VISIBLE
                    btnShowMore.setOnClickListener {
                        adapter.showAllItems()
                        btnShowMore.visibility = View.GONE
                        btnShowLess.visibility = View.VISIBLE
                    }
                    
                    btnShowLess.setOnClickListener {
                        adapter.showFirstItems(3)
                        btnShowMore.visibility = View.VISIBLE
                        btnShowLess.visibility = View.GONE
                    }
                } else {
                    btnShowMore.visibility = View.GONE
                    btnShowLess.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading loans/credits", e)
            }
        }
    }

    private fun loadMonthlyPayments() {
        // TODO: Implement new monthly payments loading logic
        // MonthlyPaymentsAdapter was removed, need to implement new approach
        // This is a placeholder for future implementation
    }

    private fun updatePageIndicator() {
        val dots = listOf(
            findViewById<ImageView>(R.id.iv_dot1),
            findViewById<ImageView>(R.id.iv_dot2),
            findViewById<ImageView>(R.id.iv_dot3)
        )
        
        dots.forEachIndexed { index, dot ->
            dot.setImageResource(
                if (index == currentPage) R.drawable.ic_dot_filled
                else R.drawable.ic_dot_outline
            )
        }
    }

    private fun setupSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                // Hide status bar and navigation bar completely
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Set window flags for edge-to-edge
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }


    override fun onResume() {
        super.onResume()
        updateCardContent()
        loadTransactions()
    }
}
