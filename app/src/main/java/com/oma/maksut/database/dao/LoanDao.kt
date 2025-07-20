package com.oma.maksut.database.dao

import androidx.room.*
import com.oma.maksut.database.entities.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    
    @Query("SELECT * FROM loans WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveLoans(): Flow<List<Loan>>
    
    @Query("SELECT * FROM loans ORDER BY name ASC")
    fun getAllLoans(): Flow<List<Loan>>
    
    @Query("SELECT SUM(current_balance) FROM loans WHERE is_active = 1")
    suspend fun getTotalLoanBalance(): Double?
    
    @Query("SELECT SUM(total_repayment_amount) FROM loans WHERE is_active = 1")
    suspend fun getTotalRepaymentAmount(): Double?
    
    @Query("SELECT SUM(monthly_payment) FROM loans WHERE is_active = 1")
    suspend fun getTotalMonthlyPayments(): Double?
    
    @Query("SELECT SUM(total_repayment_amount - original_amount) FROM loans WHERE is_active = 1")
    suspend fun getTotalInterestAmount(): Double?
    
    @Query("UPDATE loans SET current_balance = current_balance - :repaymentAmount WHERE id = :loanId")
    suspend fun reduceLoanBalance(loanId: Long, repaymentAmount: Double)
    
    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: Long): Loan?
    
    @Insert
    suspend fun insertLoan(loan: Loan): Long
    
    @Update
    suspend fun updateLoan(loan: Loan)
    
    @Delete
    suspend fun deleteLoan(loan: Loan)
    
    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: Long)
}