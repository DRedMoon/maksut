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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.oma.maksut.database.entities.Category
import com.oma.maksut.repository.FinanceRepository
import com.oma.maksut.adapters.CategoryAdapter
import kotlinx.coroutines.launch
import java.util.*

class CategoryManagementActivity : AppCompatActivity() {
    
    private lateinit var repository: FinanceRepository
    private lateinit var adapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_management)
        
        repository = FinanceRepository(this)
        
        setupViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadCategories()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.rv_categories)
        fabAddCategory = findViewById(R.id.fab_add_category)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> showDeleteCategoryDialog(category) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryManagementActivity)
            adapter = this@CategoryManagementActivity.adapter
        }
    }
    
    private fun setupListeners() {
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }
    
    private fun loadCategories() {
        lifecycleScope.launch {
            repository.getAllCategories().collect { categories ->
                adapter.updateCategories(categories)
            }
        }
    }
    
    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_category_name)
        val etIcon = dialogView.findViewById<EditText>(R.id.et_category_icon)
        val cbHasDueDate = dialogView.findViewById<CheckBox>(R.id.cb_has_due_date)
        val cbIsMonthlyPayment = dialogView.findViewById<CheckBox>(R.id.cb_is_monthly_payment)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_category))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val name = etName.text.toString().trim()
                val icon = etIcon.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val category = Category(
                        name = name,
                        iconResource = icon.ifEmpty { "ic_category" },
                        color = 0xFF4CAF50.toInt(), // Default green color
                        hasDueDate = cbHasDueDate.isChecked,
                        isMonthlyPayment = cbIsMonthlyPayment.isChecked
                    )
                    
                    lifecycleScope.launch {
                        repository.insertCategory(category)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showEditCategoryDialog(category: Category) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_category_name)
        val etIcon = dialogView.findViewById<EditText>(R.id.et_category_icon)
        val cbHasDueDate = dialogView.findViewById<CheckBox>(R.id.cb_has_due_date)
        val cbIsMonthlyPayment = dialogView.findViewById<CheckBox>(R.id.cb_is_monthly_payment)
        
        // Pre-fill with existing data
        etName.setText(category.name)
        etIcon.setText(category.iconResource)
        cbHasDueDate.isChecked = category.hasDueDate
        cbIsMonthlyPayment.isChecked = category.isMonthlyPayment
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.edit_category))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = etName.text.toString().trim()
                val icon = etIcon.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val updatedCategory = category.copy(
                        name = name,
                        iconResource = icon.ifEmpty { "ic_category" },
                        hasDueDate = cbHasDueDate.isChecked,
                        isMonthlyPayment = cbIsMonthlyPayment.isChecked,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    lifecycleScope.launch {
                        repository.updateCategory(updatedCategory)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showDeleteCategoryDialog(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_category))
            .setMessage(getString(R.string.delete_category_confirmation, category.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteCategory(category)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_category_management, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_category -> {
                showAddCategoryDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}