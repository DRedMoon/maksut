package com.oma.maksut.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "original_amount")
    val originalAmount: Double,
    
    @ColumnInfo(name = "remaining_balance")
    val remainingBalance: Double,
    
    @ColumnInfo(name = "interest_rate")
    val interestRate: Double,
    
    @ColumnInfo(name = "personal_margin")
    val personalMargin: Double = 0.0,
    
    @ColumnInfo(name = "total_interest_rate")
    val totalInterestRate: Double = interestRate + personalMargin,
    
    @ColumnInfo(name = "loan_term_years")
    val loanTermYears: Int,
    
    @ColumnInfo(name = "monthly_payment_amount")
    val monthlyPaymentAmount: Double,
    
    @ColumnInfo(name = "payment_fee")
    val paymentFee: Double = 0.0,
    
    @ColumnInfo(name = "due_day")
    val dueDay: Int,
    
    @ColumnInfo(name = "due_date")
    val dueDate: Date,
    
    @ColumnInfo(name = "start_date")
    val startDate: Date,
    
    @ColumnInfo(name = "end_date")
    val endDate: Date,
    
    @ColumnInfo(name = "total_repayment_amount")
    val totalRepaymentAmount: Double,
    
    @ColumnInfo(name = "total_interest_amount")
    val totalInterestAmount: Double,
    
    @ColumnInfo(name = "is_paid")
    var isPaid: Boolean = false,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)