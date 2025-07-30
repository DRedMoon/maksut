package com.oma.maksut.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "credits")
data class Credit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double,
    
    @ColumnInfo(name = "current_balance")
    val currentBalance: Double,
    
    @ColumnInfo(name = "interest_rate")
    val interestRate: Double,
    
    @ColumnInfo(name = "personal_margin")
    val personalMargin: Double = 0.0,
    
    @ColumnInfo(name = "total_interest_rate")
    val totalInterestRate: Double = interestRate + personalMargin,
    
    @ColumnInfo(name = "minimum_payment_percentage")
    val minimumPaymentPercentage: Double,
    
    @ColumnInfo(name = "minimum_payment_amount")
    val minimumPaymentAmount: Double,
    
    @ColumnInfo(name = "payment_fee")
    val paymentFee: Double = 0.0,
    
    @ColumnInfo(name = "due_day")
    val dueDay: Int,
    
    @ColumnInfo(name = "due_date")
    val dueDate: Date,
    
    @ColumnInfo(name = "grace_period_days")
    val gracePeriodDays: Int = 0,
    
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