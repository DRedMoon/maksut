package com.oma.maksut.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    
    @ColumnInfo(name = "payment_date")
    val paymentDate: Date,
    
    @ColumnInfo(name = "due_date")
    val dueDate: Date? = null,
    
    @ColumnInfo(name = "is_monthly_payment")
    val isMonthlyPayment: Boolean = false,
    
    @ColumnInfo(name = "is_loan_repayment")
    val isLoanRepayment: Boolean = false,
    
    @ColumnInfo(name = "is_credit_repayment")
    val isCreditRepayment: Boolean = false,
    
    @ColumnInfo(name = "loan_id")
    val loanId: Long? = null,
    
    @ColumnInfo(name = "credit_id")
    val creditId: Long? = null,
    
    @ColumnInfo(name = "repayment_amount")
    val repaymentAmount: Double? = null,
    
    @ColumnInfo(name = "interest_amount")
    val interestAmount: Double? = null,
    
    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = true,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)