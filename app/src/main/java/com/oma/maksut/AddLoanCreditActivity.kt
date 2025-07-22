package com.oma.maksut

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.LinearLayout

class AddLoanCreditActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var etName: EditText
    private lateinit var etOriginalAmount: EditText
    private lateinit var etCurrentBalance: EditText
    private lateinit var etMonthlyPayment: EditText
    private lateinit var etHandlingFee: EditText
    private lateinit var etEuriborRate: EditText
    private lateinit var etPersonalMargin: EditText
    private lateinit var etMonthsLeft: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var tvTotalRepayment: TextView
    private lateinit var tvTotalInterest: TextView
    private lateinit var btnSave: Button
    private lateinit var spinnerType: Spinner
    
    private var selectedDueDate: Date = Date()
    private var isLoan = true // true for loan, false for credit
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan_credit)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupListeners()
        setupTypeSpinner()
    }
    
    private fun setupViews() {
        etName = findViewById(R.id.et_name)
        etOriginalAmount = findViewById(R.id.et_original_amount)
        etCurrentBalance = findViewById(R.id.et_current_balance)
        etMonthlyPayment = findViewById(R.id.et_monthly_payment)
        etHandlingFee = findViewById(R.id.et_handling_fee)
        etEuriborRate = findViewById(R.id.et_euribor_rate)
        etPersonalMargin = findViewById(R.id.et_personal_margin)
        etMonthsLeft = findViewById(R.id.et_months_left)
        tvDueDate = findViewById(R.id.tv_due_date)
        tvTotalRepayment = findViewById(R.id.tv_total_repayment)
        tvTotalInterest = findViewById(R.id.tv_total_interest)
        btnSave = findViewById(R.id.btn_save)
        spinnerType = findViewById(R.id.spinner_type)
        
        // Set current date as default due date
        tvDueDate.text = dateFormat.format(selectedDueDate)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupTypeSpinner() {
        val types = arrayOf("Laina", "Luotto")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                isLoan = position == 0
                updateUIForType()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                isLoan = true
            }
        }
    }
    
    private fun updateUIForType() {
        if (isLoan) {
            // Show loan-specific fields
            findViewById<LinearLayout>(R.id.ll_loan_fields).visibility = android.view.View.VISIBLE
            findViewById<LinearLayout>(R.id.ll_credit_fields).visibility = android.view.View.GONE
        } else {
            // Show credit-specific fields
            findViewById<LinearLayout>(R.id.ll_loan_fields).visibility = android.view.View.GONE
            findViewById<LinearLayout>(R.id.ll_credit_fields).visibility = android.view.View.VISIBLE
        }
        calculateTotals()
    }
    
    private fun setupListeners() {
        // Due date selection
        findViewById<LinearLayout>(R.id.ll_due_date_selector).setOnClickListener {
            showDatePickerDialog { date ->
                selectedDueDate = date
                tvDueDate.text = dateFormat.format(date)
            }
        }
        
        // Calculate totals when values change
        val calculateTotals = {
            calculateTotals()
        }
        
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateTotals() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        
        etOriginalAmount.addTextChangedListener(textWatcher)
        etCurrentBalance.addTextChangedListener(textWatcher)
        etMonthlyPayment.addTextChangedListener(textWatcher)
        etEuriborRate.addTextChangedListener(textWatcher)
        etPersonalMargin.addTextChangedListener(textWatcher)
        etMonthsLeft.addTextChangedListener(textWatcher)
        
        // Save button
        btnSave.setOnClickListener {
            saveLoanCredit()
        }
    }
    
    private fun calculateTotals() {
        try {
            val originalAmount = etOriginalAmount.text.toString().toDoubleOrNull() ?: 0.0
            val currentBalance = etCurrentBalance.text.toString().toDoubleOrNull() ?: 0.0
            val monthlyPayment = etMonthlyPayment.text.toString().toDoubleOrNull() ?: 0.0
            val euriborRate = etEuriborRate.text.toString().toDoubleOrNull() ?: 0.0
            val personalMargin = etPersonalMargin.text.toString().toDoubleOrNull() ?: 0.0
            val monthsLeft = etMonthsLeft.text.toString().toIntOrNull() ?: 0
            
            val totalInterestRate = euriborRate + personalMargin
            val totalRepayment = monthlyPayment * monthsLeft
            val totalInterest = totalRepayment - currentBalance
            
            tvTotalRepayment.text = String.format(Locale.getDefault(), "%.2f €", totalRepayment)
            tvTotalInterest.text = String.format(Locale.getDefault(), "%.2f €", totalInterest)
        } catch (e: Exception) {
            tvTotalRepayment.text = "0.00 €"
            tvTotalInterest.text = "0.00 €"
        }
    }
    
    private fun showDatePickerDialog(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun saveLoanCredit() {
        val name = etName.text.toString().trim()
        val originalAmount = etOriginalAmount.text.toString().toDoubleOrNull()
        val currentBalance = etCurrentBalance.text.toString().toDoubleOrNull()
        val monthlyPayment = etMonthlyPayment.text.toString().toDoubleOrNull()
        val handlingFee = etHandlingFee.text.toString().toDoubleOrNull() ?: 0.0
        val euriborRate = etEuriborRate.text.toString().toDoubleOrNull() ?: 0.0
        val personalMargin = etPersonalMargin.text.toString().toDoubleOrNull() ?: 0.0
        val monthsLeft = etMonthsLeft.text.toString().toIntOrNull()
        
        // Validation
        if (name.isEmpty()) {
            etName.error = getString(R.string.name_required)
            return
        }
        
        if (originalAmount == null || originalAmount <= 0) {
            etOriginalAmount.error = getString(R.string.invalid_original_amount)
            return
        }
        
        if (currentBalance == null || currentBalance <= 0) {
            etCurrentBalance.error = getString(R.string.invalid_current_balance)
            return
        }
        
        if (monthlyPayment == null || monthlyPayment <= 0) {
            etMonthlyPayment.error = getString(R.string.invalid_monthly_payment)
            return
        }
        
        if (monthsLeft == null || monthsLeft <= 0) {
            etMonthsLeft.error = getString(R.string.months_required)
            return
        }
        
        lifecycleScope.launch {
            try {
                if (isLoan) {
                    // Save loan
                    val loan = Loan(
                        name = name,
                        originalAmount = originalAmount,
                        currentBalance = currentBalance,
                        monthlyPayment = monthlyPayment,
                        handlingFee = handlingFee,
                        euriborRate = euriborRate,
                        personalMargin = personalMargin,
                        totalInterestRate = euriborRate + personalMargin,
                        loanTermYears = monthsLeft / 12,
                        remainingMonths = monthsLeft,
                        dueDay = selectedDueDate.date,
                        startDate = Date(),
                        endDate = selectedDueDate,
                        isActive = true
                    )
                    repository.insertLoan(loan)
                    Toast.makeText(this@AddLoanCreditActivity, getString(R.string.loan_saved), Toast.LENGTH_SHORT).show()
                } else {
                    // Save credit
                    val credit = Credit(
                        name = name,
                        creditLimit = originalAmount,
                        currentBalance = currentBalance,
                        minimumPaymentAmount = monthlyPayment,
                        totalInterestRate = euriborRate + personalMargin,
                        paymentFee = handlingFee,
                        dueDay = selectedDueDate.date,
                        paymentFreePeriod = 0,
                        isActive = true
                    )
                    repository.insertCredit(credit)
                    Toast.makeText(this@AddLoanCreditActivity, getString(R.string.credit_saved), Toast.LENGTH_SHORT).show()
                }
                
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddLoanCreditActivity, getString(R.string.error_saving, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
