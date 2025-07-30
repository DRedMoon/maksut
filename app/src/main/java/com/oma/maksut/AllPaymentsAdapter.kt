package com.oma.maksut

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.database.entities.Credit
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AllPaymentsAdapter(
    private var payments: List<Any>,
    private val onPaymentStatusChanged: (Any, Boolean) -> Unit
) : ListAdapter<Any, AllPaymentsAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_card, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updatePayments(newPayments: List<Any>) {
        payments = newPayments
        submitList(newPayments)
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPaymentName: TextView = itemView.findViewById(R.id.tv_payment_name)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        private val tvPaymentAmount: TextView = itemView.findViewById(R.id.tv_payment_amount)
        private val tvPaymentType: TextView = itemView.findViewById(R.id.tv_payment_type)
        private val cbPaymentPaid: CheckBox = itemView.findViewById(R.id.cb_payment_paid)

        fun bind(payment: Any) {
            when (payment) {
                is Loan -> bindLoan(payment)
                is Credit -> bindCredit(payment)
                is Transaction -> bindTransaction(payment)
            }
        }

        private fun bindLoan(loan: Loan) {
            tvPaymentName.text = loan.name
            tvPaymentAmount.text = String.format("€%.2f", loan.monthlyPaymentAmount)
            tvPaymentType.text = itemView.context.getString(R.string.loan)
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvDueDate.text = "Due: ${dateFormat.format(loan.dueDate)}"
            
            cbPaymentPaid.isChecked = loan.isPaid
            cbPaymentPaid.setOnCheckedChangeListener { _, isChecked ->
                onPaymentStatusChanged(loan, isChecked)
            }
        }

        private fun bindCredit(credit: Credit) {
            tvPaymentName.text = credit.name
            tvPaymentAmount.text = String.format("€%.2f", credit.minimumPaymentAmount)
            tvPaymentType.text = itemView.context.getString(R.string.credit)
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvDueDate.text = "Due: ${dateFormat.format(credit.dueDate)}"
            
            cbPaymentPaid.isChecked = credit.isPaid
            cbPaymentPaid.setOnCheckedChangeListener { _, isChecked ->
                onPaymentStatusChanged(credit, isChecked)
            }
        }

        private fun bindTransaction(transaction: Transaction) {
            tvPaymentName.text = transaction.name
            tvPaymentAmount.text = String.format("€%.2f", transaction.amount)
            tvPaymentType.text = "Monthly"
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dueDate = transaction.dueDate ?: transaction.paymentDate
            tvDueDate.text = "Due: ${dateFormat.format(dueDate)}"
            
            cbPaymentPaid.isChecked = transaction.isPaid
            cbPaymentPaid.setOnCheckedChangeListener { _, isChecked ->
                onPaymentStatusChanged(transaction, isChecked)
            }
        }
    }

    private class PaymentDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Loan && newItem is Loan -> oldItem.id == newItem.id
                oldItem is Credit && newItem is Credit -> oldItem.id == newItem.id
                oldItem is Transaction && newItem is Transaction -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Loan && newItem is Loan -> 
                    oldItem.name == newItem.name && 
                    oldItem.monthlyPaymentAmount == newItem.monthlyPaymentAmount &&
                    oldItem.isPaid == newItem.isPaid
                oldItem is Credit && newItem is Credit -> 
                    oldItem.name == newItem.name && 
                    oldItem.minimumPaymentAmount == newItem.minimumPaymentAmount &&
                    oldItem.isPaid == newItem.isPaid
                oldItem is Transaction && newItem is Transaction -> 
                    oldItem.name == newItem.name && 
                    oldItem.amount == newItem.amount &&
                    oldItem.isPaid == newItem.isPaid
                else -> false
            }
        }
    }
} 