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
    
    @Query("SELECT SUM(current_balance) FROM credits WHERE is_active = 1")
    suspend fun getTotalCreditBalance(): Double?
    
    @Query("SELECT SUM(credit_limit) FROM credits WHERE is_active = 1")
    suspend fun getTotalCreditLimit(): Double?
    
    @Query("SELECT SUM(minimum_payment_amount) FROM credits WHERE is_active = 1")
    suspend fun getTotalMinimumPayments(): Double?
    
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
}