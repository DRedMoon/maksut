package com.oma.maksut.database.dao

import androidx.room.*
import com.oma.maksut.database.entities.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY payment_date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE payment_date >= :startDate ORDER BY payment_date DESC")
    fun getTransactionsFromDate(startDate: Date): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE due_date >= :today ORDER BY due_date ASC")
    fun getUpcomingTransactions(today: Date): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_monthly_payment = 1 ORDER BY payment_date DESC")
    fun getMonthlyPayments(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_monthly_payment = 1 AND is_paid = 0 ORDER BY due_date ASC")
    fun getUnpaidMonthlyPayments(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_monthly_payment = 1 AND is_paid = 1 ORDER BY payment_date DESC")
    fun getPaidMonthlyPayments(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY payment_date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_loan_repayment = 1 ORDER BY payment_date DESC")
    fun getLoanRepayments(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_credit_repayment = 1 ORDER BY payment_date DESC")
    fun getCreditRepayments(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE due_date >= :startDate AND due_date <= :endDate ORDER BY due_date ASC")
    fun getUpcomingTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_loan_repayment = 1 OR is_credit_repayment = 1 OR amount > 0 OR category_id IN (SELECT id FROM categories WHERE is_monthly_payment = 1 OR name = 'Subscription' OR name = 'Expense' OR name = 'Income') ORDER BY payment_date DESC")
    fun getRealTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_paid = 0 ORDER BY due_date ASC")
    fun getUnpaidTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_paid = 1 ORDER BY payment_date DESC")
    fun getPaidTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?
    
    @Query("UPDATE transactions SET is_paid = :isPaid WHERE id = :transactionId")
    suspend fun updatePaymentStatus(transactionId: Long, isPaid: Boolean)
    
    @Query("SELECT SUM(amount) FROM transactions WHERE payment_date <= :date")
    suspend fun getBalanceUntilDate(date: Date): Double?
    
    @Query("SELECT SUM(amount) FROM transactions")
    suspend fun getBalance(): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE payment_date >= :startDate AND payment_date <= :endDate")
    suspend fun getBalanceForPeriod(startDate: Date, endDate: Date): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE is_paid = 1")
    suspend fun getTotalPaidAmount(): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE is_paid = 0")
    suspend fun getTotalUnpaidAmount(): Double?
    
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}