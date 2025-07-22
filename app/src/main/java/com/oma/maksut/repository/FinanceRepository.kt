package com.oma.maksut.repository

import android.content.Context
import com.oma.maksut.database.AppDatabase
import com.oma.maksut.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

class FinanceRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val loanDao = database.loanDao()
    private val creditDao = database.creditDao()
    
    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsFromDate(startDate: Date): Flow<List<Transaction>> = 
        transactionDao.getTransactionsFromDate(startDate)
    
    fun getUpcomingTransactions(today: Date): Flow<List<Transaction>> = 
        transactionDao.getUpcomingTransactions(today)
    
    fun getMonthlyPayments(): Flow<List<Transaction>> = transactionDao.getMonthlyPayments()
    
    fun getUnpaidMonthlyPayments(): Flow<List<Transaction>> = transactionDao.getUnpaidMonthlyPayments()
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByCategory(categoryId)
    
    fun getLoanRepayments(): Flow<List<Transaction>> = transactionDao.getLoanRepayments()
    
    fun getCreditRepayments(): Flow<List<Transaction>> = transactionDao.getCreditRepayments()
    
    fun getUpcomingTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> = 
        transactionDao.getUpcomingTransactions(startDate, endDate)
    
    suspend fun updatePaymentStatus(transactionId: Long, isPaid: Boolean) = 
        transactionDao.updatePaymentStatus(transactionId, isPaid)
    
    suspend fun getBalanceUntilDate(date: Date): Double = 
        transactionDao.getBalanceUntilDate(date) ?: 0.0
    
    suspend fun getBalanceForPeriod(startDate: Date, endDate: Date): Double = 
        transactionDao.getBalanceForPeriod(startDate, endDate) ?: 0.0
    
    suspend fun insertTransaction(transaction: Transaction): Long = 
        transactionDao.insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: Transaction) = 
        transactionDao.updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: Transaction) = 
        transactionDao.deleteTransaction(transaction)
    
    fun getRealTransactions(): Flow<List<Transaction>> = transactionDao.getRealTransactions()
    
    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    fun getMonthlyPaymentCategories(): Flow<List<Category>> = 
        categoryDao.getMonthlyPaymentCategories()
    
    fun getCategoriesWithDueDate(): Flow<List<Category>> = 
        categoryDao.getCategoriesWithDueDate()
    
    suspend fun getCategoryById(categoryId: Long): Category? = 
        categoryDao.getCategoryById(categoryId)
    
    suspend fun insertCategory(category: Category): Long = 
        categoryDao.insertCategory(category)
    
    suspend fun updateCategory(category: Category) = 
        categoryDao.updateCategory(category)
    
    suspend fun deleteCategory(category: Category) = 
        categoryDao.deleteCategory(category)
    
    suspend fun initializeDefaultCategories() {
        val count = categoryDao.getCategoryCount()
        android.util.Log.d("FinanceRepository", "Category count: $count")
        // Force initialization if we have less than 5 categories (default categories)
        if (count < 5) {
            android.util.Log.d("FinanceRepository", "Creating default categories")
            // Insert default categories
            val defaultCategories = listOf(
                Category(
                    name = "Income",
                    iconResource = "💰",
                    color = 0xFF4CAF50.toInt(),
                    hasDueDate = false,
                    isMonthlyPayment = false,
                    isLoanRepayment = false,
                    isCreditRepayment = false
                ),
                Category(
                    name = "Expense",
                    iconResource = "💸",
                    color = 0xFFF44336.toInt(),
                    hasDueDate = false,
                    isMonthlyPayment = false,
                    isLoanRepayment = false,
                    isCreditRepayment = false
                ),
                Category(
                    name = "Subscription",
                    iconResource = "📱",
                    color = 0xFF2196F3.toInt(),
                    hasDueDate = true,
                    isMonthlyPayment = true,
                    isLoanRepayment = false,
                    isCreditRepayment = false
                ),
                Category(
                    name = "Loan Repayment",
                    iconResource = "🏦",
                    color = 0xFFFF9800.toInt(),
                    hasDueDate = true,
                    isMonthlyPayment = false,
                    isLoanRepayment = true,
                    isCreditRepayment = false
                ),
                Category(
                    name = "Credit Repayment",
                    iconResource = "💳",
                    color = 0xFF9C27B0.toInt(),
                    hasDueDate = true,
                    isMonthlyPayment = false,
                    isLoanRepayment = false,
                    isCreditRepayment = true
                )
            )
            
            defaultCategories.forEach { category ->
                val id = categoryDao.insertCategory(category)
                android.util.Log.d("FinanceRepository", "Created category: ${category.name} with ID: $id")
            }
            android.util.Log.d("FinanceRepository", "Default categories created successfully")
        } else {
            android.util.Log.d("FinanceRepository", "Categories already exist, skipping initialization")
        }
    }
    
    // Loan operations
    fun getAllActiveLoans(): Flow<List<Loan>> = loanDao.getAllActiveLoans()
    
    fun getAllLoans(): Flow<List<Loan>> = loanDao.getAllLoans()
    
    suspend fun getTotalLoanBalance(): Double = loanDao.getTotalLoanBalance() ?: 0.0
    
    suspend fun getTotalLoanRepaymentAmount(): Double = loanDao.getTotalRepaymentAmount() ?: 0.0
    
    suspend fun getTotalLoanMonthlyPayments(): Double = loanDao.getTotalMonthlyPayments() ?: 0.0
    
    suspend fun getTotalLoanInterestAmount(): Double = loanDao.getTotalInterestAmount() ?: 0.0
    
    suspend fun reduceLoanBalance(loanId: Long, repaymentAmount: Double) = 
        loanDao.reduceLoanBalance(loanId, repaymentAmount)
    
    suspend fun getLoanById(loanId: Long): Loan? = loanDao.getLoanById(loanId)
    
    suspend fun insertLoan(loan: Loan): Long = loanDao.insertLoan(loan)
    
    suspend fun updateLoan(loan: Loan) = loanDao.updateLoan(loan)
    
    suspend fun deleteLoan(loan: Loan) = loanDao.deleteLoan(loan)
    
    // Credit operations
    fun getAllActiveCredits(): Flow<List<Credit>> = creditDao.getAllActiveCredits()
    
    fun getAllCredits(): Flow<List<Credit>> = creditDao.getAllCredits()
    
    suspend fun getTotalCreditBalance(): Double = creditDao.getTotalCreditBalance() ?: 0.0
    
    suspend fun getTotalCreditLimit(): Double = creditDao.getTotalCreditLimit() ?: 0.0
    
    suspend fun getTotalCreditMinimumPayments(): Double = creditDao.getTotalMinimumPayments() ?: 0.0
    
    suspend fun getTotalCreditInterestAmount(): Double = creditDao.getTotalInterestAmount() ?: 0.0
    
    suspend fun reduceCreditBalance(creditId: Long, repaymentAmount: Double) = 
        creditDao.reduceCreditBalance(creditId, repaymentAmount)
    
    suspend fun getCreditById(creditId: Long): Credit? = creditDao.getCreditById(creditId)
    
    suspend fun insertCredit(credit: Credit): Long = creditDao.insertCredit(credit)
    
    suspend fun updateCredit(credit: Credit) = creditDao.updateCredit(credit)
    
    suspend fun deleteCredit(credit: Credit) = creditDao.deleteCredit(credit)
    
    // Combined operations
    suspend fun getTotalDebt(): Double = getTotalLoanBalance() + getTotalCreditBalance()
    
    suspend fun getTotalDebtRepayment(): Double = getTotalLoanRepaymentAmount() + getTotalCreditLimit()
    
    suspend fun getTotalMonthlyDebtPayments(): Double = 
        getTotalLoanMonthlyPayments() + getTotalCreditMinimumPayments()
}