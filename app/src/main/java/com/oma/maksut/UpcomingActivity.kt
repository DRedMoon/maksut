package com.oma.maksut

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class UpcomingActivity : AppCompatActivity() {
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    private lateinit var tvMonthTitle: TextView
    private lateinit var tvYearTitle: TextView
    private lateinit var tvMonthlyPaymentsWeek: TextView
    private lateinit var tvLoansCreditsWeek: TextView
    private lateinit var tvWeekTotal: TextView
    private lateinit var tvMonthlyPaymentsMonth: TextView
    private lateinit var tvLoansCreditsMonth: TextView
    private lateinit var tvMonthTotal: TextView
    private lateinit var tvMonthTotalLabel: TextView
    private lateinit var tvYearTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming)

        try {
            val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { finish() }
        } catch (e: Exception) {
            android.util.Log.e("UpcomingActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error loading upcoming", Toast.LENGTH_SHORT).show()
            finish()
        }

        initializeViews()
        setupNavigationButtons()
        setupFilterButtons()

        // Start with "This week" view
        showView(R.id.ll_this_week)
        loadThisWeekData()
    }

    private fun initializeViews() {
        tvMonthTitle = findViewById(R.id.tv_month_title)
        tvYearTitle = findViewById(R.id.tv_year_title)
        tvMonthlyPaymentsWeek = findViewById(R.id.tv_monthly_payments_week)
        tvLoansCreditsWeek = findViewById(R.id.tv_loans_credits_week)
        tvWeekTotal = findViewById(R.id.tv_week_total)
        tvMonthlyPaymentsMonth = findViewById(R.id.tv_monthly_payments_month)
        tvLoansCreditsMonth = findViewById(R.id.tv_loans_credits_month)
        tvMonthTotal = findViewById(R.id.tv_month_total)
        tvMonthTotalLabel = findViewById(R.id.tv_month_total_label)
        tvYearTotal = findViewById(R.id.tv_year_total)

        // Set initial month and year titles
        updateMonthTitle()
        updateYearTitle()
    }

    private fun setupNavigationButtons() {
        // Month navigation
        findViewById<Button>(R.id.btn_prev_month).setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateMonthTitle()
            loadMonthData()
        }

        findViewById<Button>(R.id.btn_next_month).setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            updateMonthTitle()
            loadMonthData()
        }

        // Year navigation
        findViewById<Button>(R.id.btn_prev_year).setOnClickListener {
            currentYear--
            updateYearTitle()
            loadYearData()
        }

        findViewById<Button>(R.id.btn_next_year).setOnClickListener {
            currentYear++
            updateYearTitle()
            loadYearData()
        }
    }

    private fun setupFilterButtons() {
        findViewById<Button>(R.id.btn_this_week).setOnClickListener {
            showView(R.id.ll_this_week)
            loadThisWeekData()
        }

        findViewById<Button>(R.id.btn_month).setOnClickListener {
            showView(R.id.ll_month)
            loadMonthData()
        }

        findViewById<Button>(R.id.btn_year).setOnClickListener {
            showView(R.id.ll_year)
            loadYearData()
        }
    }

    private fun updateMonthTitle() {
        val monthNames = arrayOf(
            getString(R.string.january), getString(R.string.february), getString(R.string.march),
            getString(R.string.april), getString(R.string.may), getString(R.string.june),
            getString(R.string.july), getString(R.string.august), getString(R.string.september),
            getString(R.string.october), getString(R.string.november), getString(R.string.december)
        )
        tvMonthTitle.text = "${monthNames[currentMonth]} $currentYear"
    }

    private fun updateYearTitle() {
        tvYearTitle.text = currentYear.toString()
    }

    private fun showView(viewId: Int) {
        // Hide all views
        findViewById<LinearLayout>(R.id.ll_this_week).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.ll_month).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.ll_year).visibility = android.view.View.GONE

        // Show selected view
        findViewById<LinearLayout>(viewId).visibility = android.view.View.VISIBLE
    }

    private fun loadThisWeekData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)

                // Load monthly payments for this week
                val monthlyPayments = repo.getMonthlyPayments().first()
                if (monthlyPayments.isNotEmpty()) {
                    val paymentsText = monthlyPayments.joinToString("\n") {
                        "• ${it.name}: ${String.format("%.2f €", it.amount)}"
                    }
                    tvMonthlyPaymentsWeek.text = paymentsText
                } else {
                    tvMonthlyPaymentsWeek.text = getString(R.string.no_payments)
                }

                // Load loans and credits for this week
                val loans = repo.getAllLoans().first()
                val credits = repo.getAllCredits().first()
                val loansCredits = mutableListOf<String>()

                loans.forEach { loan ->
                    loansCredits.add("• ${loan.name}: ${String.format("%.2f €", loan.monthlyPayment)}")
                }
                credits.forEach { credit ->
                    val creditPayment = credit.minimumPaymentAmount
                    loansCredits.add("• ${credit.name}: ${String.format("%.2f €", creditPayment)}")
                }

                if (loansCredits.isNotEmpty()) {
                    tvLoansCreditsWeek.text = loansCredits.joinToString("\n")
                } else {
                    tvLoansCreditsWeek.text = getString(R.string.no_payments)
                }

                // Calculate total for this week
                val total = monthlyPayments.sumOf { it.amount } +
                        loans.sumOf { it.monthlyPayment } +
                        credits.sumOf { it.minimumPaymentAmount }
                tvWeekTotal.text = String.format("%.2f €", total)

            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading this week data", e)
                tvMonthlyPaymentsWeek.text = getString(R.string.no_payments)
                tvLoansCreditsWeek.text = getString(R.string.no_payments)
                tvWeekTotal.text = getString(R.string.zero_euro)
            }
        }
    }

    private fun loadMonthData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)

                // Load monthly payments for selected month
                val monthlyPayments = repo.getMonthlyPayments().first()
                if (monthlyPayments.isNotEmpty()) {
                    val paymentsText = monthlyPayments.joinToString("\n") {
                        "• ${it.name}: ${String.format("%.2f €", it.amount)}"
                    }
                    tvMonthlyPaymentsMonth.text = paymentsText
                } else {
                    tvMonthlyPaymentsMonth.text = getString(R.string.no_payments)
                }

                // Load loans and credits for selected month
                val loans = repo.getAllLoans().first()
                val credits = repo.getAllCredits().first()
                val loansCredits = mutableListOf<String>()

                loans.forEach { loan ->
                    loansCredits.add("• ${loan.name}: ${String.format("%.2f €", loan.monthlyPayment)}")
                }
                credits.forEach { credit ->
                    val creditPayment = credit.minimumPaymentAmount
                    loansCredits.add("• ${credit.name}: ${String.format("%.2f €", creditPayment)}")
                }

                if (loansCredits.isNotEmpty()) {
                    tvLoansCreditsMonth.text = loansCredits.joinToString("\n")
                } else {
                    tvLoansCreditsMonth.text = getString(R.string.no_payments)
                }

                // Calculate total for selected month
                val total = monthlyPayments.sumOf { it.amount } +
                        loans.sumOf { it.monthlyPayment } +
                        credits.sumOf { it.minimumPaymentAmount }
                tvMonthTotal.text = String.format("%.2f €", total)

            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading month data", e)
                tvMonthlyPaymentsMonth.text = getString(R.string.no_payments)
                tvLoansCreditsMonth.text = getString(R.string.no_payments)
                tvMonthTotal.text = getString(R.string.zero_euro)
            }
        }
    }

    private fun loadYearData() {
        lifecycleScope.launch {
            try {
                val repo = com.oma.maksut.repository.FinanceRepository(this@UpcomingActivity)

                // Calculate total for selected year (monthly payments * 12 + loans/credits * 12)
                val monthlyPayments = repo.getMonthlyPayments().first()
                val loans = repo.getAllLoans().first()
                val credits = repo.getAllCredits().first()

                val total = (monthlyPayments.sumOf { it.amount } +
                        loans.sumOf { it.monthlyPayment } +
                        credits.sumOf { it.minimumPaymentAmount }) * 12

                tvYearTotal.text = String.format("%.2f €", total)

            } catch (e: Exception) {
                android.util.Log.e("UpcomingActivity", "Error loading year data", e)
                tvYearTotal.text = getString(R.string.zero_euro)
            }
        }
    }
}