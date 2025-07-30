package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import java.text.SimpleDateFormat
import java.util.*

// This data class is now defined in MainActivity.kt

class LoanCreditExpandedAdapter(
    private var items: List<com.oma.maksut.LoanCreditItem>
) : RecyclerView.Adapter<LoanCreditExpandedAdapter.LoanCreditViewHolder>() {
    
    private var allItems = items
    private var displayedItems = if (items.size > 3) items.take(3) else items
    
    fun showAllItems() {
        displayedItems = allItems
        notifyDataSetChanged()
    }
    
    fun showFirstItems(count: Int) {
        displayedItems = allItems.take(count)
        notifyDataSetChanged()
    }
    

    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanCreditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_credit_expanded, parent, false)
        return LoanCreditViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LoanCreditViewHolder, position: Int) {
        holder.bind(displayedItems[position])
    }
    
    override fun getItemCount(): Int = displayedItems.size
    
    inner class LoanCreditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_loan_credit_name)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_loan_credit_amount)
        private val tvInterestRate: TextView = itemView.findViewById(R.id.tv_loan_credit_interest_rate)
        private val tvTotalInterest: TextView = itemView.findViewById(R.id.tv_loan_credit_total_interest)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_loan_credit_total_amount)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_loan_credit_due_date)
        private val tvType: TextView = itemView.findViewById(R.id.tv_loan_credit_type)
        
        fun bind(item: com.oma.maksut.LoanCreditItem) {
            tvName.text = item.name
            tvAmount.text = String.format(Locale.getDefault(), "%.2f €", item.amount)
            tvInterestRate.text = String.format(Locale.getDefault(), "%.2f%%", item.interestRate)
            tvTotalInterest.text = String.format(Locale.getDefault(), "%.2f €", item.totalInterest)
            tvTotalAmount.text = String.format(Locale.getDefault(), "%.2f €", item.totalAmount)
            tvType.text = item.type
            
            tvDueDate.text = item.dueDate
            tvDueDate.visibility = View.VISIBLE
        }
    }
} 