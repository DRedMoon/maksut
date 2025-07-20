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
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByCategory(categoryId)
    
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
    
    // Loan operations
    fun getAllActiveLoans(): Flow<List<Loan>> = loanDao.getAllActiveLoans()
    
    fun getAllLoans(): Flow<List<Loan>> = loanDao.getAllLoans()
    
    suspend fun getTotalLoanBalance(): Double = loanDao.getTotalLoanBalance() ?: 0.0
    
    suspend fun getTotalLoanRepaymentAmount(): Double = loanDao.getTotalRepaymentAmount() ?: 0.0
    
    suspend fun getTotalLoanMonthlyPayments(): Double = loanDao.getTotalMonthlyPayments() ?: 0.0
    
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