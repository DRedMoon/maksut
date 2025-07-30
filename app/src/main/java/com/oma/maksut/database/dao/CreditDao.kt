package com.oma.maksut.database.dao

import androidx.room.*
import com.oma.maksut.database.entities.Credit
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    
    @Query("SELECT * FROM credits WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveCredits(): Flow<List<Credit>>
    
    @Query("SELECT * FROM credits ORDER BY name ASC")
    fun getAllCredits(): Flow<List<Credit>>
    
    @Query("SELECT * FROM credits WHERE is_active = 1 AND is_paid = 0 ORDER BY due_date ASC")
    fun getUnpaidCredits(): Flow<List<Credit>>
    
    @Query("SELECT * FROM credits WHERE is_active = 1 AND is_paid = 1 ORDER BY due_date ASC")
    fun getPaidCredits(): Flow<List<Credit>>
    
    @Query("SELECT SUM(current_balance) FROM credits WHERE is_active = 1")
    suspend fun getTotalCreditBalance(): Double?
    
    @Query("SELECT SUM(total_repayment_amount) FROM credits WHERE is_active = 1")
    suspend fun getTotalRepaymentAmount(): Double?
    
    @Query("SELECT SUM(minimum_payment_amount) FROM credits WHERE is_active = 1")
    suspend fun getTotalMinimumPayments(): Double?
    
    @Query("SELECT SUM(total_interest_amount) FROM credits WHERE is_active = 1")
    suspend fun getTotalInterestAmount(): Double?
    
    @Query("SELECT SUM(minimum_payment_amount) FROM credits WHERE is_active = 1 AND is_paid = 1")
    suspend fun getTotalPaidMinimumPayments(): Double?
    
    @Query("SELECT SUM(minimum_payment_amount) FROM credits WHERE is_active = 1 AND is_paid = 0")
    suspend fun getTotalUnpaidMinimumPayments(): Double?
    
    @Query("UPDATE credits SET current_balance = current_balance - :repaymentAmount WHERE id = :creditId")
    suspend fun reduceCreditBalance(creditId: Long, repaymentAmount: Double)
    
    @Query("UPDATE credits SET is_paid = :isPaid WHERE id = :creditId")
    suspend fun updateCreditPaymentStatus(creditId: Long, isPaid: Boolean)
    
    @Query("SELECT * FROM credits WHERE id = :creditId")
    suspend fun getCreditById(creditId: Long): Credit?
    
    @Insert
    suspend fun insertCredit(credit: Credit): Long
    
    @Update
    suspend fun updateCredit(credit: Credit)
    
    @Delete
    suspend fun deleteCredit(credit: Credit)
    
    @Query("DELETE FROM credits WHERE id = :creditId")
    suspend fun deleteCreditById(creditId: Long)
    
    @Query("DELETE FROM credits")
    suspend fun deleteAllCredits()
}