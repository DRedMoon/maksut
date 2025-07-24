package com.oma.maksut

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.oma.maksut.database.entities.Category
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class QuickAddTransactionActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private var selectedCategory: Category? = null
    private var selectedPaymentDate: Date = Date()
    private var selectedDueDate: Date? = null
    
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDueDate: TextView
    private lateinit var cbIsMonthlyPayment: CheckBox
    private lateinit var cbHasDueDate: CheckBox
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var llAmountSection: LinearLayout
    
    // Loan/Credit repayment fields
    private lateinit var llLoanSelection: LinearLayout
    private lateinit var llCreditSelection: LinearLayout
    private lateinit var etRepaymentAmount: EditText
    private lateinit var etInterestAmount: EditText
    private lateinit var etCreditRepaymentAmount: EditText
    private lateinit var etCreditInterestAmount: EditText
    private lateinit var spinnerLoanSelection: Spinner
    private lateinit var spinnerCreditSelection: Spinner
    private var selectedLoan: Loan? = null
    private var selectedCredit: Credit? = null
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_add_transaction)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupListeners()
        loadCategories()
    }
    
    private fun setupViews() {
        etName = findViewById(R.id.et_transaction_name)
        etAmount = findViewById(R.id.et_transaction_amount)
        spinnerCategory = findViewById(R.id.spinner_category)
        tvDueDate = findViewById(R.id.tv_due_date)
        cbIsMonthlyPayment = findViewById(R.id.cb_is_monthly_payment)
        cbHasDueDate = findViewById(R.id.cb_has_due_date)
        rgTransactionType = findViewById(R.id.rg_transaction_type)
        btnSave = findViewById(R.id.btn_save_transaction)
        llAmountSection = findViewById(R.id.ll_amount_section)

        // Loan/Credit repayment fields
        llLoanSelection = findViewById(R.id.ll_loan_selection)
        llCreditSelection = findViewById(R.id.ll_credit_selection)
        etRepaymentAmount = findViewById(R.id.et_repayment_amount)
        etInterestAmount = findViewById(R.id.et_interest_amount)
        etCreditRepaymentAmount = findViewById(R.id.et_credit_repayment_amount)
        etCreditInterestAmount = findViewById(R.id.et_credit_interest_amount)
        spinnerLoanSelection = findViewById(R.id.spinner_loan_selection)
        spinnerCreditSelection = findViewById(R.id.spinner_credit_selection)
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_quick_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_manage_categories -> {
                startActivity(Intent(this, CategoryManagementActivity::class.java))
                true
            }
            R.id.action_all_payments -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupListeners() {
        // Category spinner
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) { // Skip "Select category" option
                    lifecycleScope.launch {
                        val categories = repository.getAllCategories().first()
                        if (position - 1 < categories.size) {
                            selectedCategory = categories[position - 1]
                            updateLayoutForCategory()
                        }
                    }
                } else {
                    selectedCategory = null
                    updateLayoutForCategory()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
                updateLayoutForCategory()
            }
        }

        // Loan selection dropdown
        spinnerLoanSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) { // Skip "Select Loan" option
                    lifecycleScope.launch {
                        val loans = repository.getAllActiveLoans().first()
                        if (position - 1 < loans.size) {
                            selectedLoan = loans[position - 1]
                        }
                    }
                } else {
                    selectedLoan = null
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedLoan = null
            }
        }
        
        // Credit selection dropdown
        spinnerCreditSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) { // Skip "Select Credit" option
                    lifecycleScope.launch {
                        val credits = repository.getAllActiveCredits().first()
                        if (position - 1 < credits.size) {
                            selectedCredit = credits[position - 1]
                        }
                    }
                } else {
                    selectedCredit = null
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCredit = null
            }
        }

        // Due date selection
        findViewById<LinearLayout>(R.id.ll_due_date_selector).setOnClickListener {
            showDatePickerDialog { date ->
                selectedDueDate = date
                tvDueDate.text = dateFormat.format(date)
            }
        }
        
        // Save button
        btnSave.setOnClickListener {
            saveTransaction()
        }
        
        // Checkbox listeners
        cbHasDueDate.setOnCheckedChangeListener { _, isChecked ->
            findViewById<LinearLayout>(R.id.ll_due_date_selector).visibility =
                if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            if (!isChecked) {
                selectedDueDate = null
                tvDueDate.text = getString(R.string.select_due_date)
            }
        }
    }
    
    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                // Initialize default categories first
                repository.initializeDefaultCategories()
                // Load categories
                val categories = repository.getAllCategories().first()
                val categoryNames = listOf(getString(R.string.select_category)) + categories.map { it.name }
                val adapter = ArrayAdapter(this@QuickAddTransactionActivity, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionActivity", "Error loading categories", e)
            }
        }
    }

    private fun updateLayoutForCategory() {
        selectedCategory?.let { category ->
            // Show/hide amount section based on category type
            if (category.isLoanRepayment || category.isCreditRepayment) {
                llAmountSection.visibility = android.view.View.GONE
            } else {
                llAmountSection.visibility = android.view.View.VISIBLE
            }
            
            // Show/hide loan/credit selection based on category type
            if (category.isLoanRepayment) {
                llLoanSelection.visibility = android.view.View.VISIBLE
                llCreditSelection.visibility = android.view.View.GONE
                loadLoanDropdown()
            } else if (category.isCreditRepayment) {
                llLoanSelection.visibility = android.view.View.GONE
                llCreditSelection.visibility = android.view.View.VISIBLE
                loadCreditDropdown()
            } else {
                llLoanSelection.visibility = android.view.View.GONE
                llCreditSelection.visibility = android.view.View.GONE
            }

            //Update checkboxes based on category settings
            cbHasDueDate.isChecked = category.hasDueDate
            cbIsMonthlyPayment.isChecked = category.isMonthlyPayment
        } ?: run {
            // No category selected
            llAmountSection.visibility = android.view.View.VISIBLE
            llLoanSelection.visibility = android.view.View.GONE
            llCreditSelection.visibility = android.view.View.GONE
        }
    }
    
    private fun loadLoanDropdown() {
        lifecycleScope.launch {
            try {
                val loans = repository.getAllActiveLoans().first()
                val loanNames = listOf(getString(R.string.select_loan)) + loans.map { "${it.name} - €${String.format(Locale.getDefault(), "%.2f", it.currentBalance)}" }
                val adapter = ArrayAdapter(this@QuickAddTransactionActivity, android.R.layout.simple_spinner_item, loanNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerLoanSelection.adapter = adapter
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionActivity", "Error loading loans", e)
            }
        }
    }
    
    private fun loadCreditDropdown() {
        lifecycleScope.launch {
            try {
                val credits = repository.getAllActiveCredits().first()
                val creditNames = listOf(getString(R.string.select_credit)) + credits.map { "${it.name} - €${String.format(Locale.getDefault(), "%.2f", it.currentBalance)}" }
                val adapter = ArrayAdapter(this@QuickAddTransactionActivity, android.R.layout.simple_spinner_item, creditNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCreditSelection.adapter = adapter
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionActivity", "Error loading credits", e)
            }
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
    
    private fun saveTransaction() {
        val name = etName.text.toString().trim()
        
        if (name.isEmpty()) {
            etName.error = getString(R.string.enter_transaction_name)
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, getString(R.string.select_category), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate loan/credit selection for repayment categories
        if (selectedCategory!!.isLoanRepayment && selectedLoan == null) {
            Toast.makeText(this, getString(R.string.select_loan), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedCategory!!.isCreditRepayment && selectedCredit == null) {
            Toast.makeText(this, getString(R.string.select_credit), Toast.LENGTH_SHORT).show()
            return
        }

        // Get amount based on category type
        val amount = when {
            selectedCategory!!.isLoanRepayment -> {
                val repaymentStr = etRepaymentAmount.text.toString().trim()
                val interestStr = etInterestAmount.text.toString().trim()
                if (repaymentStr.isEmpty() && interestStr.isEmpty()) {
                    Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show()
                    return
                }
                (repaymentStr.toDoubleOrNull() ?: 0.0) + (interestStr.toDoubleOrNull() ?: 0.0)
            }
            selectedCategory!!.isCreditRepayment -> {
                val repaymentStr = etCreditRepaymentAmount.text.toString().trim()
                val interestStr = etCreditInterestAmount.text.toString().trim()
                if (repaymentStr.isEmpty() && interestStr.isEmpty()) {
                    Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show()
                    return
                }
                (repaymentStr.toDoubleOrNull() ?: 0.0) + (interestStr.toDoubleOrNull() ?: 0.0)
            }
            else -> {
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isEmpty()) {
                    etAmount.error = getString(R.string.enter_amount)
                    return
                }
                amountStr.toDoubleOrNull() ?: 0.0
            }
        }

        if (amount == 0.0) {
            Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Determine if it's income or expense based on radio button
        val finalAmount = when (rgTransactionType.checkedRadioButtonId) {
            R.id.rb_income -> amount
            else -> -amount
        }
        
        val transaction = Transaction(
            name = name,
            amount = finalAmount,
            categoryId = selectedCategory!!.id,
            paymentDate = selectedPaymentDate,
            dueDate = selectedDueDate,
            isMonthlyPayment = cbIsMonthlyPayment.isChecked,
            isLoanRepayment = selectedCategory!!.isLoanRepayment,
            isCreditRepayment = selectedCategory!!.isCreditRepayment,
            loanId = selectedLoan?.id,
            creditId = selectedCredit?.id,
            repaymentAmount = when {
                selectedCategory!!.isLoanRepayment -> etRepaymentAmount.text.toString().toDoubleOrNull()
                selectedCategory!!.isCreditRepayment -> etCreditRepaymentAmount.text.toString().toDoubleOrNull()
                else -> null
            },
            interestAmount = when {
                selectedCategory!!.isLoanRepayment -> etInterestAmount.text.toString().toDoubleOrNull()
                selectedCategory!!.isCreditRepayment -> etCreditInterestAmount.text.toString().toDoubleOrNull()
                else -> null
            },
            description = null
        )
        
        lifecycleScope.launch {
            try {
                val transactionId = repository.insertTransaction(transaction)
                Toast.makeText(this@QuickAddTransactionActivity,
                    getString(R.string.save_transaction), Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@QuickAddTransactionActivity, 
                    getString(R.string.error_saving_transaction), Toast.LENGTH_SHORT).show()
            }
        }
    }

}