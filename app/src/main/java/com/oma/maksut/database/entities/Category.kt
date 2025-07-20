package com.oma.maksut.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "icon_resource")
    val iconResource: String,
    
    @ColumnInfo(name = "color")
    val color: Int,
    
    @ColumnInfo(name = "has_due_date")
    val hasDueDate: Boolean = false,
    
    @ColumnInfo(name = "is_monthly_payment")
    val isMonthlyPayment: Boolean = false,
    
    @ColumnInfo(name = "is_loan_repayment")
    val isLoanRepayment: Boolean = false,
    
    @ColumnInfo(name = "is_credit_repayment")
    val isCreditRepayment: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)