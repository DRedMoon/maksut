package com.oma.maksut

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.view.Menu
import android.view.MenuItem

class AllTransactionsActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var adapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerTransactionType: AutoCompleteTextView
    private lateinit var spinnerTimeline: AutoCompleteTextView
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var etSearchTransactions: EditText
    private lateinit var tvEmptyState: TextView
    
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var selectedTransactionType: String = "all"
    private var selectedTimeline: String = "all"
    private var searchQuery: String = ""
    private var allTransactions: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)
        
        try {
            repository = FinanceRepository(this)
            
            setupViews()
            setupToolbar()
            setupDropdowns()
            setupRecyclerView()
            setupDateButtons()
            loadTransactions()
        } catch (e: Exception) {
            android.util.Log.e("AllTransactionsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupViews() {
        spinnerTransactionType = findViewById(R.id.spinner_transaction_type)
        spinnerTimeline = findViewById(R.id.spinner_timeline)
        btnStartDate = findViewById(R.id.btn_start_date)
        btnEndDate = findViewById(R.id.btn_end_date)
        recyclerView = findViewById(R.id.rv_transactions)
        etSearchTransactions = findViewById(R.id.et_search)
        tvEmptyState = findViewById(R.id.tv_empty_state)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupDropdowns() {
        // Transaction type dropdown
        val transactionTypes = arrayOf(
            getString(R.string.all_transactions),
            getString(R.string.loans),
            getString(R.string.credits)
        )
        val transactionTypeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, transactionTypes)
        spinnerTransactionType.setAdapter(transactionTypeAdapter)
        spinnerTransactionType.setOnItemClickListener { _, _, position, _ ->
            selectedTransactionType = when (position) {
                0 -> "all"
                1 -> "loans"
                2 -> "credits"
                else -> "all"
            }
            filterTransactions()
        }
        
        // Timeline dropdown
        val timelineOptions = arrayOf(
            getString(R.string.timeline),
            getString(R.string.last_week),
            getString(R.string.last_month),
            getString(R.string.last_quarter),
            getString(R.string.last_year)
        )
        val timelineAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, timelineOptions)
        spinnerTimeline.setAdapter(timelineAdapter)
        spinnerTimeline.setOnItemClickListener { _, _, position, _ ->
            selectedTimeline = when (position) {
                0 -> "all"
                1 -> "last_week"
                2 -> "last_month"
                3 -> "last_quarter"
                4 -> "last_year"
                else -> "all"
            }
            applyTimelineFilter()
            filterTransactions()
        }
        
        // Setup search functionality
        etSearchTransactions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                filterTransactions()
            }
        })
    }
    
    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllTransactionsActivity)
            adapter = this@AllTransactionsActivity.adapter
        }
    }
    
    private fun setupDateButtons() {
        btnStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                btnStartDate.text = getString(R.string.starting_point) + ": " + dateFormat.format(date)
                filterTransactions()
            }
        }
        
        btnEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                btnEndDate.text = getString(R.string.ending_point) + ": " + dateFormat.format(date)
                filterTransactions()
            }
        }
    }
    
    private fun applyTimelineFilter() {
        val calendar = Calendar.getInstance()
        when (selectedTimeline) {
            "last_week" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                startDate = calendar.time
                endDate = Date()
            }
            "last_month" -> {
                calendar.add(Calendar.MONTH, -1)
                startDate = calendar.time
                endDate = Date()
            }
            "last_quarter" -> {
                calendar.add(Calendar.MONTH, -3)
                startDate = calendar.time
                endDate = Date()
            }
            "last_year" -> {
                calendar.add(Calendar.YEAR, -1)
                startDate = calendar.time
                endDate = Date()
            }
            else -> {
                startDate = null
                endDate = null
            }
        }
        
        // Update button texts
        btnStartDate.text = startDate?.let { getString(R.string.starting_point) + ": " + dateFormat.format(it) } ?: getString(R.string.starting_point)
        btnEndDate.text = endDate?.let { getString(R.string.ending_point) + ": " + dateFormat.format(it) } ?: getString(R.string.ending_point)
    }
    
    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            R.style.DatePickerDialogTheme,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                allTransactions = repository.getRealTransactions().first()
                filterTransactions()
            } catch (e: Exception) {
                android.util.Log.e("AllTransactionsActivity", "Error loading transactions", e)
                Toast.makeText(this@AllTransactionsActivity, "Error loading transactions", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun filterTransactions() {
        val searchQuery = etSearchTransactions.text.toString().trim()
        val selectedType = selectedTransactionType
        val selectedTimeline = selectedTimeline
        
        lifecycleScope.launch {
            try {
                var filtered = allTransactions.toMutableList()
                
                // Filter by search query
                if (searchQuery.isNotEmpty()) {
                    filtered = filtered.filter { transaction ->
                        transaction.name.contains(searchQuery, ignoreCase = true) ||
                        transaction.description?.contains(searchQuery, ignoreCase = true) == true
                    }.toMutableList()
                }
                
                // Filter by transaction type
                if (selectedType != "all") {
                    filtered = filtered.filter { transaction ->
                        when (selectedType) {
                            "loans" -> transaction.isLoanRepayment
                            "credits" -> transaction.isCreditRepayment
                            else -> true
                        }
                    }.toMutableList()
                }
                
                // Filter by timeline
                val filteredByTimeline = when (selectedTimeline) {
                    "last_week" -> filterByDateRange(7)
                    "last_month" -> filterByDateRange(30)
                    "last_quarter" -> filterByDateRange(90)
                    "last_year" -> filterByDateRange(365)
                    else -> filtered
                }
                
                // Apply date range filter if set
                val finalFiltered = if (startDate != null && endDate != null) {
                    filteredByTimeline.filter { transaction ->
                        transaction.paymentDate.after(startDate) && transaction.paymentDate.before(endDate)
                    }
                } else {
                    filteredByTimeline
                }
                
                adapter.updateItems(finalFiltered)
                updateTransactionCount(finalFiltered.size)
                
            } catch (e: Exception) {
                android.util.Log.e("AllTransactionsActivity", "Error filtering transactions", e)
            }
        }
    }
    
    private fun filterByDateRange(days: Int): List<Transaction> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        return allTransactions.filter { it.paymentDate.after(cutoffDate) }
    }
    
    private fun updateTransactionCount(count: Int) {
        // tvTransactionCount.text = getString(R.string.transactions_found, count) // This line is removed
        tvEmptyState.visibility = if (count == 0) View.VISIBLE else View.GONE
    }
} 