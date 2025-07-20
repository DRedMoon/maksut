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
    
    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY payment_date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE payment_date <= :date")
    suspend fun getBalanceUntilDate(date: Date): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE payment_date >= :startDate AND payment_date <= :endDate")
    suspend fun getBalanceForPeriod(startDate: Date, endDate: Date): Double?
    
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)
}