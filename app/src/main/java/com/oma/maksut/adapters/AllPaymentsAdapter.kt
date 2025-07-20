package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AllPaymentsAdapter(
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit,
    private val onTogglePaid: (Transaction, Boolean) -> Unit
) : RecyclerView.Adapter<AllPaymentsAdapter.PaymentViewHolder>() {
    
    private var payments: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun updatePayments(newPayments: List<Transaction>) {
        payments = newPayments.sortedByDescending { it.paymentDate }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_card, parent, false)
        return PaymentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }
    
    override fun getItemCount(): Int = payments.size
    
    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_payment_name)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_payment_amount)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_payment_date)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_payment_category)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_payment_due_date)
        private val ivPaidStatus: ImageView = itemView.findViewById(R.id.iv_paid_status)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_payment)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_payment)
        
        fun bind(transaction: Transaction) {
            tvName.text = transaction.name
            tvAmount.text = String.format(Locale.getDefault(), "%.2f €", transaction.amount)
            tvDate.text = dateFormat.format(transaction.paymentDate)
            
            // Set amount color based on positive/negative
            tvAmount.setTextColor(
                if (transaction.amount >= 0) 
                    itemView.context.getColor(android.R.color.holo_green_light)
                else 
                    itemView.context.getColor(android.R.color.holo_red_light)
            )
            
            // Show category if available
            tvCategory.text = "Category" // TODO: Get category name from database
            tvCategory.visibility = View.VISIBLE
            
            // Show due date if available
            if (transaction.dueDate != null) {
                tvDueDate.text = "Due: ${dateFormat.format(transaction.dueDate)}"
                tvDueDate.visibility = View.VISIBLE
            } else {
                tvDueDate.visibility = View.GONE
            }
            
            // Show paid status
            if (transaction.isMonthlyPayment) {
                ivPaidStatus.setImageResource(
                    if (transaction.isPaid) R.drawable.ic_check_circle 
                    else R.drawable.ic_radio_button_unchecked
                )
                ivPaidStatus.visibility = View.VISIBLE
                ivPaidStatus.setOnClickListener {
                    onTogglePaid(transaction, !transaction.isPaid)
                }
            } else {
                ivPaidStatus.visibility = View.GONE
            }
            
            // Show special indicators for loan/credit repayments
            if (transaction.isLoanRepayment) {
                tvCategory.text = "Loan Repayment"
                if (transaction.repaymentAmount != null && transaction.interestAmount != null) {
                    tvCategory.text = "Loan Repayment (${transaction.repaymentAmount}€ + ${transaction.interestAmount}€ interest)"
                }
            } else if (transaction.isCreditRepayment) {
                tvCategory.text = "Credit Repayment"
                if (transaction.repaymentAmount != null && transaction.interestAmount != null) {
                    tvCategory.text = "Credit Repayment (${transaction.repaymentAmount}€ + ${transaction.interestAmount}€ interest)"
                }
            }
            
            btnEdit.setOnClickListener { onEditClick(transaction) }
            btnDelete.setOnClickListener { onDeleteClick(transaction) }
        }
    }
}