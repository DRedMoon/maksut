package com.oma.maksut

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AnalysisActivity : AppCompatActivity() {
    
    private val fmtIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fmtDot = SimpleDateFormat("dd.M.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        
        // Setup toolbar
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
        
        // Setup analysis buttons
        findViewById<LinearLayout>(R.id.btn_monthly_analysis).setOnClickListener {
            showAnalysis("monthly")
        }
        
        findViewById<LinearLayout>(R.id.btn_yearly_analysis).setOnClickListener {
            showAnalysis("yearly")
        }
        
        findViewById<LinearLayout>(R.id.btn_full_analysis).setOnClickListener {
            showAnalysis("full")
        }
    }
    
    private fun showAnalysis(type: String) {
        val transactions = TransactionRepository.transactions
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)
        
        val filteredTransactions = when (type) {
            "monthly" -> {
                transactions.filter { tx ->
                    val date = parseDate(tx.time)
                    date?.let { 
                        val cal = Calendar.getInstance().apply { time = it }
                        cal.get(Calendar.MONTH) == currentMonth && 
                        cal.get(Calendar.YEAR) == currentYear
                    } ?: false
                }
            }
            "yearly" -> {
                transactions.filter { tx ->
                    val date = parseDate(tx.time)
                    date?.let { 
                        val cal = Calendar.getInstance().apply { time = it }
                        cal.get(Calendar.YEAR) == currentYear
                    } ?: false
                }
            }
            else -> transactions // full analysis
        }
        
        val income = filteredTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenses = filteredTransactions.filter { it.amount < 0 }.sumOf { -it.amount }
        val netBalance = income - expenses
        
        val title = when (type) {
            "monthly" -> getString(R.string.monthly_analysis)
            "yearly" -> getString(R.string.yearly_analysis)
            else -> getString(R.string.full_analysis)
        }
        
        val message = buildString {
            appendLine("${getString(R.string.total_income)}: ${String.format(Locale.getDefault(), "%.2f €", income)}")
            appendLine("${getString(R.string.total_expenses)}: ${String.format(Locale.getDefault(), "%.2f €", expenses)}")
            appendLine("${getString(R.string.net_balance)}: ${String.format(Locale.getDefault(), "%.2f €", netBalance)}")
            appendLine()
            appendLine("Tapahtumia: ${filteredTransactions.size}")
        }
        
        // Update the analysis display
        findViewById<TextView>(R.id.tv_analysis_title).text = title
        findViewById<TextView>(R.id.tv_analysis_content).text = message
        
        // Update the analysis container visibility
        findViewById<LinearLayout>(R.id.ll_analysis_result).visibility = android.view.View.VISIBLE
    }
    
    private fun parseDate(dateStr: String): Date? {
        return runCatching { fmtIso.parse(dateStr) }.getOrNull()
            ?: runCatching { fmtDot.parse(dateStr) }.getOrNull()
    }
}