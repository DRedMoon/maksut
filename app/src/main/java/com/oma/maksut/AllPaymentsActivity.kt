package com.oma.maksut

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
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.repository.FinanceRepository
import com.oma.maksut.adapters.AllPaymentsAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AllPaymentsActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var adapter: AllPaymentsAdapter
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_payments)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupRecyclerView()
        loadPayments()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.rv_all_payments)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = AllPaymentsAdapter(
            onEditClick = { transaction -> showEditPaymentDialog(transaction) },
            onDeleteClick = { transaction -> showDeletePaymentDialog(transaction) },
            onTogglePaid = { transaction, isPaid -> togglePaymentStatus(transaction, isPaid) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllPaymentsActivity)
            adapter = this@AllPaymentsActivity.adapter
        }
    }
    
    private fun loadPayments() {
        lifecycleScope.launch {
            repository.getAllTransactions().collect { transactions ->
                adapter.updatePayments(transactions)
            }
        }
    }
    
    private fun showEditPaymentDialog(transaction: Transaction) {
        // Navigate to QuickAddTransactionActivity with edit mode
        val intent = Intent(this, QuickAddTransactionActivity::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("TRANSACTION_ID", transaction.id)
        }
        startActivity(intent)
    }
    
    private fun showDeletePaymentDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_payment))
            .setMessage(getString(R.string.delete_payment_confirmation, transaction.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteTransaction(transaction)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun togglePaymentStatus(transaction: Transaction, isPaid: Boolean) {
        lifecycleScope.launch {
            repository.updatePaymentStatus(transaction.id, isPaid)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all_payments, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_payment -> {
                startActivity(Intent(this, QuickAddTransactionActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}