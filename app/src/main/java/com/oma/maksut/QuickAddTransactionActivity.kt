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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oma.maksut.database.entities.Category
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.launch
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
    private lateinit var tvPaymentDate: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var cbIsMonthlyPayment: CheckBox
    private lateinit var cbHasDueDate: CheckBox
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var btnSave: Button
    
    // Loan/Credit repayment fields
    private lateinit var llLoanSelection: LinearLayout
    private lateinit var llCreditSelection: LinearLayout
    private lateinit var tvSelectedLoan: TextView
    private lateinit var tvSelectedCredit: TextView
    private lateinit var etRepaymentAmount: EditText
    private lateinit var etInterestAmount: EditText
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
        loadDefaultCategory()
    }
    
    private fun setupViews() {
        etName = findViewById(R.id.et_transaction_name)
        etAmount = findViewById(R.id.et_transaction_amount)
        spinnerCategory = findViewById(R.id.spinner_category)
        tvPaymentDate = findViewById(R.id.tv_payment_date)
        tvDueDate = findViewById(R.id.tv_due_date)
        cbIsMonthlyPayment = findViewById(R.id.cb_is_monthly_payment)
        cbHasDueDate = findViewById(R.id.cb_has_due_date)
        rgTransactionType = findViewById(R.id.rg_transaction_type)
        btnSave = findViewById(R.id.btn_save_transaction)
        
        // Loan/Credit repayment fields
        llLoanSelection = findViewById(R.id.ll_loan_selection)
        llCreditSelection = findViewById(R.id.ll_credit_selection)
        tvSelectedLoan = findViewById(R.id.tv_selected_loan)
        tvSelectedCredit = findViewById(R.id.tv_selected_credit)
        etRepaymentAmount = findViewById(R.id.et_repayment_amount)
        etInterestAmount = findViewById(R.id.et_interest_amount)
        
        // Set current date as default
        tvPaymentDate.text = dateFormat.format(selectedPaymentDate)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupListeners() {
        // Category selection
        findViewById<LinearLayout>(R.id.ll_category_selector).setOnClickListener {
            showCategorySelectionDialog()
        }
        
        // Loan selection
        llLoanSelection.setOnClickListener {
            showLoanSelectionDialog()
        }
        
        // Credit selection
        llCreditSelection.setOnClickListener {
            showCreditSelectionDialog()
        }
        
        // Payment date selection
        findViewById<LinearLayout>(R.id.ll_payment_date_selector).setOnClickListener {
            showDatePickerDialog { date ->
                selectedPaymentDate = date
                tvPaymentDate.text = dateFormat.format(date)
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
                tvDueDate.text = ""
            }
        }
        
        cbIsMonthlyPayment.setOnCheckedChangeListener { _, isChecked ->
            // Update category if needed
            selectedCategory?.let { category ->
                if (category.isMonthlyPayment != isChecked) {
                    showCategorySelectionDialog()
                }
            }
        }
    }
    
    private fun loadDefaultCategory() {
        lifecycleScope.launch {
            // Load first available category as default
            repository.getAllCategories().collect { categories ->
                if (categories.isNotEmpty()) {
                    selectedCategory = categories.first()
                    updateCategoryDisplay()
                }
            }
        }
    }
    
    private fun showCategorySelectionDialog() {
        lifecycleScope.launch {
            repository.getAllCategories().collect { categories ->
                val categoryNames = categories.map { it.name }.toTypedArray()
                
                MaterialAlertDialogBuilder(this@QuickAddTransactionActivity)
                    .setTitle(getString(R.string.select_category))
                    .setItems(categoryNames) { _, which ->
                        selectedCategory = categories[which]
                        updateCategoryDisplay()
                        
                        // Update checkboxes based on category settings
                        selectedCategory?.let { category ->
                            cbHasDueDate.isChecked = category.hasDueDate
                            cbIsMonthlyPayment.isChecked = category.isMonthlyPayment
                        }
                    }
                    .setNeutralButton(getString(R.string.manage_categories)) { _, _ ->
                        startActivity(Intent(this@QuickAddTransactionActivity, CategoryManagementActivity::class.java))
                    }
                    .show()
            }
        }
    }
    
    private fun updateCategoryDisplay() {
        selectedCategory?.let { category ->
            // Replace spinnerCategory.setSelection(category.name) with logic to find the index of the category in the spinner adapter and setSelection(index)
            val adapter = spinnerCategory.adapter as? ArrayAdapter<String>
            val index = adapter?.getPosition(category.name) ?: -1
            spinnerCategory.setSelection(index)
            
            // Show/hide loan/credit selection based on category type
            if (category.isLoanRepayment) {
                llLoanSelection.visibility = android.view.View.VISIBLE
                llCreditSelection.visibility = android.view.View.GONE
            } else if (category.isCreditRepayment) {
                llLoanSelection.visibility = android.view.View.GONE
                llCreditSelection.visibility = android.view.View.VISIBLE
            } else {
                llLoanSelection.visibility = android.view.View.GONE
                llCreditSelection.visibility = android.view.View.GONE
            }
        } ?: run {
            spinnerCategory.setSelection(R.string.select_category)
            llLoanSelection.visibility = android.view.View.GONE
            llCreditSelection.visibility = android.view.View.GONE
        }
    }
    
    private fun showLoanSelectionDialog() {
        lifecycleScope.launch {
            repository.getAllActiveLoans().collect { loans ->
                if (loans.isNotEmpty()) {
                    val loanNames = loans.map { it.name }.toTypedArray()
                    
                    MaterialAlertDialogBuilder(this@QuickAddTransactionActivity)
                        .setTitle(getString(R.string.select_loan))
                        .setItems(loanNames) { _, which ->
                            selectedLoan = loans[which]
                            tvSelectedLoan.text = selectedLoan!!.name
                        }
                        .show()
                } else {
                    Toast.makeText(this@QuickAddTransactionActivity, 
                        "No loans available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showCreditSelectionDialog() {
        lifecycleScope.launch {
            repository.getAllActiveCredits().collect { credits ->
                if (credits.isNotEmpty()) {
                    val creditNames = credits.map { it.name }.toTypedArray()
                    
                    MaterialAlertDialogBuilder(this@QuickAddTransactionActivity)
                        .setTitle(getString(R.string.select_credit))
                        .setItems(creditNames) { _, which ->
                            selectedCredit = credits[which]
                            tvSelectedCredit.text = selectedCredit!!.name
                        }
                        .show()
                } else {
                    Toast.makeText(this@QuickAddTransactionActivity, 
                        "No credits available", Toast.LENGTH_SHORT).show()
                }
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
        val amountStr = etAmount.text.toString().trim()
        
        if (name.isEmpty()) {
            etName.error = getString(R.string.name_required)
            return
        }
        
        if (amountStr.isEmpty()) {
            etAmount.error = getString(R.string.amount_required)
            return
        }
        
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount == 0.0) {
            etAmount.error = getString(R.string.invalid_amount)
            return
        }
        
        if (selectedCategory == null) {
            Toast.makeText(this, getString(R.string.category_required), Toast.LENGTH_SHORT).show()
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
            repaymentAmount = etRepaymentAmount.text.toString().toDoubleOrNull(),
            interestAmount = etInterestAmount.text.toString().toDoubleOrNull(),
            description = null
        )
        
        lifecycleScope.launch {
            try {
                val transactionId = repository.insertTransaction(transaction)
                
                // Handle loan/credit balance reduction
                if (transaction.isLoanRepayment && selectedLoan != null && transaction.repaymentAmount != null) {
                    repository.reduceLoanBalance(selectedLoan!!.id, transaction.repaymentAmount)
                    Toast.makeText(this@QuickAddTransactionActivity, 
                        getString(R.string.loan_balance_reduced), Toast.LENGTH_SHORT).show()
                } else if (transaction.isCreditRepayment && selectedCredit != null && transaction.repaymentAmount != null) {
                    repository.reduceCreditBalance(selectedCredit!!.id, transaction.repaymentAmount)
                    Toast.makeText(this@QuickAddTransactionActivity, 
                        getString(R.string.credit_balance_reduced), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@QuickAddTransactionActivity, 
                        getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show()
                }
                
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@QuickAddTransactionActivity, 
                    getString(R.string.error_saving_transaction), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
                startActivity(Intent(this, AllPaymentsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}