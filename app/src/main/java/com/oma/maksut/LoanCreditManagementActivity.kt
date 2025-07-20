package com.oma.maksut

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.repository.FinanceRepository
import com.oma.maksut.adapters.LoanAdapter
import com.oma.maksut.adapters.CreditAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LoanCreditManagementActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var loanAdapter: LoanAdapter
    private lateinit var creditAdapter: CreditAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTotalRepayment: TextView
    private lateinit var tvTotalInterest: TextView
    
    private var showingRepaymentAmount = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_credit_management)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupTabLayout()
        setupRecyclerView()
        setupListeners()
        loadData()
    }
    
    private fun setupViews() {
        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.rv_items)
        fabAdd = findViewById(R.id.fab_add)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        tvTotalRepayment = findViewById(R.id.tv_total_repayment)
        tvTotalInterest = findViewById(R.id.tv_total_interest)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.loans)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.credits)))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showLoans()
                    1 -> showCredits()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        loanAdapter = LoanAdapter(
            onEditClick = { loan -> showEditLoanDialog(loan) },
            onDeleteClick = { loan -> showDeleteLoanDialog(loan) }
        )
        
        creditAdapter = CreditAdapter(
            onEditClick = { credit -> showEditCreditDialog(credit) },
            onDeleteClick = { credit -> showDeleteCreditDialog(credit) }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = loanAdapter // Default to loans
    }
    
    private fun setupListeners() {
        fabAdd.setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                0 -> showAddLoanDialog()
                1 -> showAddCreditDialog()
            }
        }
        
        // Click on total amount to toggle between balance and repayment
        findViewById<LinearLayout>(R.id.ll_total_amount).setOnClickListener {
            showingRepaymentAmount = !showingRepaymentAmount
            updateTotalDisplay()
        }
    }
    
    private fun loadData() {
        lifecycleScope.launch {
            // Load loans
            repository.getAllActiveLoans().collect { loans ->
                loanAdapter.updateLoans(loans)
                updateTotalDisplay()
            }
        }
        
        lifecycleScope.launch {
            // Load credits
            repository.getAllActiveCredits().collect { credits ->
                creditAdapter.updateCredits(credits)
                updateTotalDisplay()
            }
        }
    }
    
    private fun showLoans() {
        recyclerView.adapter = loanAdapter
        fabAdd.setOnClickListener { showAddLoanDialog() }
    }
    
    private fun showCredits() {
        recyclerView.adapter = creditAdapter
        fabAdd.setOnClickListener { showAddCreditDialog() }
    }
    
    private fun updateTotalDisplay() {
        lifecycleScope.launch {
            if (showingRepaymentAmount) {
                val totalRepayment = repository.getTotalDebtRepayment()
                tvTotalAmount.text = getString(R.string.total_repayment_amount, totalRepayment)
                tvTotalRepayment.text = getString(R.string.click_to_show_balance)
            } else {
                val totalDebt = repository.getTotalDebt()
                tvTotalAmount.text = getString(R.string.total_debt_amount, totalDebt)
                tvTotalRepayment.text = getString(R.string.click_to_show_repayment)
            }
            
            // Always show total interest
            val totalLoanInterest = repository.getTotalLoanInterestAmount()
            val totalCreditInterest = repository.getTotalCreditInterestAmount()
            val totalInterest = totalLoanInterest + totalCreditInterest
            tvTotalInterest.text = getString(R.string.total_interest_amount, totalInterest)
        }
    }
    
    private fun showAddLoanDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_loan, null)
        setupLoanDialog(dialogView, null)
    }
    
    private fun showEditLoanDialog(loan: Loan) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_loan, null)
        setupLoanDialog(dialogView, loan)
    }
    
    private fun setupLoanDialog(dialogView: android.view.View, existingLoan: Loan?) {
        val etName = dialogView.findViewById<EditText>(R.id.et_loan_name)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_loan_amount)
        val etInterestRate = dialogView.findViewById<EditText>(R.id.et_interest_rate)
        val etPersonalMargin = dialogView.findViewById<EditText>(R.id.et_personal_margin)
        val etLoanTerm = dialogView.findViewById<EditText>(R.id.et_loan_term)
        val etMonthlyPayment = dialogView.findViewById<EditText>(R.id.et_monthly_payment)
        val etPaymentFee = dialogView.findViewById<EditText>(R.id.et_payment_fee)
        val etDueDay = dialogView.findViewById<EditText>(R.id.et_due_day)
        val tvStartDate = dialogView.findViewById<TextView>(R.id.tv_start_date)
        val tvEndDate = dialogView.findViewById<TextView>(R.id.tv_end_date)
        val tvTotalRepayment = dialogView.findViewById<TextView>(R.id.tv_calculated_repayment)
        
        var startDate = Date()
        var endDate = Date()
        
        // Pre-fill if editing
        existingLoan?.let { loan ->
            etName.setText(loan.name)
            etAmount.setText(loan.originalAmount.toString())
            etInterestRate.setText(loan.interestRate.toString())
            etPersonalMargin.setText(loan.personalMargin.toString())
            etLoanTerm.setText(loan.loanTermYears.toString())
            etMonthlyPayment.setText(loan.monthlyPayment.toString())
            etPaymentFee.setText(loan.paymentFee.toString())
            etDueDay.setText(loan.dueDay.toString())
            startDate = loan.startDate
            endDate = loan.endDate
            tvStartDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startDate)
            tvEndDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate)
            tvTotalRepayment.text = getString(R.string.total_repayment, loan.totalRepaymentAmount)
        }
        
        // Date pickers
        dialogView.findViewById<LinearLayout>(R.id.ll_start_date).setOnClickListener {
            showDatePickerDialog { date ->
                startDate = date
                tvStartDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
                calculateLoanRepayment(etAmount, etInterestRate, etPersonalMargin, etLoanTerm, tvTotalRepayment)
            }
        }
        
        dialogView.findViewById<LinearLayout>(R.id.ll_end_date).setOnClickListener {
            showDatePickerDialog { date ->
                endDate = date
                tvEndDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
            }
        }
        
        // Calculate repayment when values change
        val calculateRepayment = {
            calculateLoanRepayment(etAmount, etInterestRate, etPersonalMargin, etLoanTerm, tvTotalRepayment)
        }
        
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateRepayment() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        etInterestRate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateRepayment() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        etPersonalMargin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateRepayment() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        etLoanTerm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateRepayment() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        MaterialAlertDialogBuilder(this)
            .setTitle(if (existingLoan != null) getString(R.string.edit_loan) else getString(R.string.add_loan))
            .setView(dialogView)
            .setPositiveButton(if (existingLoan != null) getString(R.string.save) else getString(R.string.add)) { _, _ ->
                saveLoan(
                    existingLoan,
                    etName.text.toString(),
                    etAmount.text.toString(),
                    etInterestRate.text.toString(),
                    etPersonalMargin.text.toString(),
                    etLoanTerm.text.toString(),
                    etMonthlyPayment.text.toString(),
                    etPaymentFee.text.toString(),
                    etDueDay.text.toString(),
                    startDate,
                    endDate,
                    tvTotalRepayment.text.toString()
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun calculateLoanRepayment(
        etAmount: EditText,
        etInterestRate: EditText,
        etPersonalMargin: EditText,
        etLoanTerm: EditText,
        tvTotalRepayment: TextView
    ) {
        try {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val interestRate = etInterestRate.text.toString().toDoubleOrNull() ?: 0.0
            val personalMargin = etPersonalMargin.text.toString().toDoubleOrNull() ?: 0.0
            val loanTerm = etLoanTerm.text.toString().toIntOrNull() ?: 0
            
            if (amount > 0 && loanTerm > 0) {
                val totalInterestRate = (interestRate + personalMargin) / 100
                val monthlyRate = totalInterestRate / 12
                val numberOfPayments = loanTerm * 12
                
                val monthlyPayment = if (monthlyRate > 0) {
                    amount * (monthlyRate * Math.pow(1 + monthlyRate, numberOfPayments.toDouble())) / 
                    (Math.pow(1 + monthlyRate, numberOfPayments.toDouble()) - 1)
                } else {
                    amount / numberOfPayments
                }
                
                val totalRepayment = monthlyPayment * numberOfPayments
                tvTotalRepayment.text = getString(R.string.total_repayment, totalRepayment)
            }
        } catch (e: Exception) {
            tvTotalRepayment.text = getString(R.string.calculation_error)
        }
    }
    
    private fun saveLoan(
        existingLoan: Loan?,
        name: String,
        amountStr: String,
        interestRateStr: String,
        personalMarginStr: String,
        loanTermStr: String,
        monthlyPaymentStr: String,
        paymentFeeStr: String,
        dueDayStr: String,
        startDate: Date,
        endDate: Date,
        totalRepaymentStr: String
    ) {
        if (name.isEmpty() || amountStr.isEmpty() || interestRateStr.isEmpty() || 
            loanTermStr.isEmpty() || monthlyPaymentStr.isEmpty() || dueDayStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val amount = amountStr.toDouble()
            val interestRate = interestRateStr.toDouble()
            val personalMargin = personalMarginStr.toDoubleOrNull() ?: 0.0
            val loanTerm = loanTermStr.toInt()
            val monthlyPayment = monthlyPaymentStr.toDouble()
            val paymentFee = paymentFeeStr.toDoubleOrNull() ?: 0.0
            val dueDay = dueDayStr.toInt()
            val totalRepayment = totalRepaymentStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            
            val loan = existingLoan?.copy(
                name = name,
                originalAmount = amount,
                currentBalance = amount, // For new loans, current balance equals original amount
                interestRate = interestRate,
                personalMargin = personalMargin,
                totalInterestRate = interestRate + personalMargin,
                loanTermYears = loanTerm,
                monthlyPayment = monthlyPayment,
                paymentFee = paymentFee,
                dueDay = dueDay,
                startDate = startDate,
                endDate = endDate,
                totalRepaymentAmount = totalRepayment,
                updatedAt = Date()
            ) ?: Loan(
                name = name,
                originalAmount = amount,
                currentBalance = amount,
                interestRate = interestRate,
                personalMargin = personalMargin,
                totalInterestRate = interestRate + personalMargin,
                loanTermYears = loanTerm,
                monthlyPayment = monthlyPayment,
                paymentFee = paymentFee,
                dueDay = dueDay,
                startDate = startDate,
                endDate = endDate,
                totalRepaymentAmount = totalRepayment
            )
            
            lifecycleScope.launch {
                if (existingLoan != null) {
                    repository.updateLoan(loan)
                } else {
                    repository.insertLoan(loan)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invalid_values), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAddCreditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_credit, null)
        setupCreditDialog(dialogView, null)
    }
    
    private fun showEditCreditDialog(credit: Credit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_credit, null)
        setupCreditDialog(dialogView, credit)
    }
    
    private fun setupCreditDialog(dialogView: android.view.View, existingCredit: Credit?) {
        val etName = dialogView.findViewById<EditText>(R.id.et_credit_name)
        val etCreditLimit = dialogView.findViewById<EditText>(R.id.et_credit_limit)
        val etCurrentBalance = dialogView.findViewById<EditText>(R.id.et_current_balance)
        val etInterestRate = dialogView.findViewById<EditText>(R.id.et_interest_rate)
        val etPersonalMargin = dialogView.findViewById<EditText>(R.id.et_personal_margin)
        val etMinPaymentPercentage = dialogView.findViewById<EditText>(R.id.et_min_payment_percentage)
        val etMinPaymentAmount = dialogView.findViewById<EditText>(R.id.et_min_payment_amount)
        val etPaymentFee = dialogView.findViewById<EditText>(R.id.et_payment_fee)
        val etDueDay = dialogView.findViewById<EditText>(R.id.et_due_day)
        val etGracePeriod = dialogView.findViewById<EditText>(R.id.et_grace_period)
        
        // Pre-fill if editing
        existingCredit?.let { credit ->
            etName.setText(credit.name)
            etCreditLimit.setText(credit.creditLimit.toString())
            etCurrentBalance.setText(credit.currentBalance.toString())
            etInterestRate.setText(credit.interestRate.toString())
            etPersonalMargin.setText(credit.personalMargin.toString())
            etMinPaymentPercentage.setText(credit.minimumPaymentPercentage.toString())
            etMinPaymentAmount.setText(credit.minimumPaymentAmount.toString())
            etPaymentFee.setText(credit.paymentFee.toString())
            etDueDay.setText(credit.dueDay.toString())
            etGracePeriod.setText(credit.gracePeriodDays.toString())
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(if (existingCredit != null) getString(R.string.edit_credit) else getString(R.string.add_credit))
            .setView(dialogView)
            .setPositiveButton(if (existingCredit != null) getString(R.string.save) else getString(R.string.add)) { _, _ ->
                saveCredit(
                    existingCredit,
                    etName.text.toString(),
                    etCreditLimit.text.toString(),
                    etCurrentBalance.text.toString(),
                    etInterestRate.text.toString(),
                    etPersonalMargin.text.toString(),
                    etMinPaymentPercentage.text.toString(),
                    etMinPaymentAmount.text.toString(),
                    etPaymentFee.text.toString(),
                    etDueDay.text.toString(),
                    etGracePeriod.text.toString()
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun saveCredit(
        existingCredit: Credit?,
        name: String,
        creditLimitStr: String,
        currentBalanceStr: String,
        interestRateStr: String,
        personalMarginStr: String,
        minPaymentPercentageStr: String,
        minPaymentAmountStr: String,
        paymentFeeStr: String,
        dueDayStr: String,
        gracePeriodStr: String
    ) {
        if (name.isEmpty() || creditLimitStr.isEmpty() || currentBalanceStr.isEmpty() || 
            interestRateStr.isEmpty() || minPaymentPercentageStr.isEmpty() || 
            minPaymentAmountStr.isEmpty() || dueDayStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val creditLimit = creditLimitStr.toDouble()
            val currentBalance = currentBalanceStr.toDouble()
            val interestRate = interestRateStr.toDouble()
            val personalMargin = personalMarginStr.toDoubleOrNull() ?: 0.0
            val minPaymentPercentage = minPaymentPercentageStr.toDouble()
            val minPaymentAmount = minPaymentAmountStr.toDouble()
            val paymentFee = paymentFeeStr.toDoubleOrNull() ?: 0.0
            val dueDay = dueDayStr.toInt()
            val gracePeriod = gracePeriodStr.toIntOrNull() ?: 0
            
            val credit = existingCredit?.copy(
                name = name,
                creditLimit = creditLimit,
                currentBalance = currentBalance,
                interestRate = interestRate,
                personalMargin = personalMargin,
                totalInterestRate = interestRate + personalMargin,
                minimumPaymentPercentage = minPaymentPercentage,
                minimumPaymentAmount = minPaymentAmount,
                paymentFee = paymentFee,
                dueDay = dueDay,
                gracePeriodDays = gracePeriod,
                updatedAt = Date()
            ) ?: Credit(
                name = name,
                creditLimit = creditLimit,
                currentBalance = currentBalance,
                interestRate = interestRate,
                personalMargin = personalMargin,
                totalInterestRate = interestRate + personalMargin,
                minimumPaymentPercentage = minPaymentPercentage,
                minimumPaymentAmount = minPaymentAmount,
                paymentFee = paymentFee,
                dueDay = dueDay,
                gracePeriodDays = gracePeriod
            )
            
            lifecycleScope.launch {
                if (existingCredit != null) {
                    repository.updateCredit(credit)
                } else {
                    repository.insertCredit(credit)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invalid_values), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDeleteLoanDialog(loan: Loan) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_loan))
            .setMessage(getString(R.string.delete_loan_confirmation, loan.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteLoan(loan)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showDeleteCreditDialog(credit: Credit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_credit))
            .setMessage(getString(R.string.delete_credit_confirmation, credit.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteCredit(credit)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_loan_credit_management, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_loan -> {
                showAddLoanDialog()
                true
            }
            R.id.action_add_credit -> {
                showAddCreditDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}