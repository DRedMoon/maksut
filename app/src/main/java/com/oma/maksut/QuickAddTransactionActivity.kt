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
    private lateinit var tvCategory: TextView
    private lateinit var tvPaymentDate: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var cbIsMonthlyPayment: CheckBox
    private lateinit var cbHasDueDate: CheckBox
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var btnSave: Button
    
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
        tvCategory = findViewById(R.id.tv_selected_category)
        tvPaymentDate = findViewById(R.id.tv_payment_date)
        tvDueDate = findViewById(R.id.tv_due_date)
        cbIsMonthlyPayment = findViewById(R.id.cb_is_monthly_payment)
        cbHasDueDate = findViewById(R.id.cb_has_due_date)
        rgTransactionType = findViewById(R.id.rg_transaction_type)
        btnSave = findViewById(R.id.btn_save_transaction)
        
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
            tvCategory.text = category.name
        } ?: run {
            tvCategory.text = getString(R.string.select_category)
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
            description = null
        )
        
        lifecycleScope.launch {
            try {
                repository.insertTransaction(transaction)
                Toast.makeText(this@QuickAddTransactionActivity, 
                    getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show()
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}