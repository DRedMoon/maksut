package com.oma.maksut

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.oma.maksut.repository.FinanceRepository

class AnalysisActivity : AppCompatActivity() {
    private val fmtIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fmtDot = SimpleDateFormat("dd.M.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
        findViewById<LinearLayout>(R.id.btn_monthly_analysis).setOnClickListener { showAnalysis("monthly") }
        findViewById<LinearLayout>(R.id.btn_yearly_analysis).setOnClickListener { showAnalysis("yearly") }
        findViewById<LinearLayout>(R.id.btn_full_analysis).setOnClickListener { showAnalysis("full") }
    }

    private fun showAnalysis(type: String) {
        lifecycleScope.launch {
            val repository = FinanceRepository(this@AnalysisActivity)
            val transactions = repository.getRealTransactions().first()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)
            val filteredTransactions = when (type) {
                "monthly" -> transactions.filter { tx ->
                    val cal = Calendar.getInstance().apply { time = tx.paymentDate }
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
                "yearly" -> transactions.filter { tx ->
                    val cal = Calendar.getInstance().apply { time = tx.paymentDate }
                    cal.get(Calendar.YEAR) == currentYear
                }
                else -> transactions
            }
            val income = filteredTransactions.filter { it.amount > 0 }.sumOf { it.amount }
            val expenses = filteredTransactions.filter { it.amount < 0 }.sumOf { -it.amount }
            val netBalance = income - expenses
            val title = when (type) {
                "monthly" -> getString(R.string.monthly_analysis)
                "yearly" -> getString(R.string.yearly_analysis)
                else -> getString(R.string.full_analysis)
            }
            val message = getString(
                R.string.analysis_message,
                income,
                expenses,
                netBalance,
                filteredTransactions.size
            )
            findViewById<TextView>(R.id.tv_analysis_title).text = title
            findViewById<TextView>(R.id.tv_analysis_content).text = message
            findViewById<LinearLayout>(R.id.ll_analysis_result).visibility = android.view.View.VISIBLE
        }
    }

    private fun parseDate(dateStr: String): Date? {
        return runCatching { fmtIso.parse(dateStr) }.getOrNull()
            ?: runCatching { fmtDot.parse(dateStr) }.getOrNull()
    }
}