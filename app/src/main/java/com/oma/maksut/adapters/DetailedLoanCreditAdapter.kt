package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import java.util.*
import java.text.SimpleDateFormat

class DetailedLoanCreditAdapter(
    private var loans: List<Loan>,
    private var credits: List<Credit>,
    private val onEditLoan: (Loan) -> Unit,
    private val onDeleteLoan: (Loan) -> Unit,
    private val onEditCredit: (Credit) -> Unit,
    private val onDeleteCredit: (Credit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LOAN = 0
        private const val VIEW_TYPE_CREDIT = 1
    }

    // Loan ViewHolder
    class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_loan_credit_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_loan_credit_type)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_loan_credit_amount)
        val tvProgressPercentage: TextView = itemView.findViewById(R.id.tv_progress_percentage)
        val tvRemainingBalance: TextView = itemView.findViewById(R.id.tv_remaining_balance)
        val tvMonthlyPayment: TextView = itemView.findViewById(R.id.tv_monthly_payment)
        val tvInterestRate: TextView = itemView.findViewById(R.id.tv_interest_rate)
        val tvTotalRepayment: TextView = itemView.findViewById(R.id.tv_total_repayment)
        val tvTotalInterest: TextView = itemView.findViewById(R.id.tv_total_interest)
        val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    // Credit ViewHolder
    class CreditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_loan_credit_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_loan_credit_type)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_loan_credit_amount)
        val tvProgressPercentage: TextView = itemView.findViewById(R.id.tv_progress_percentage)
        val tvRemainingBalance: TextView = itemView.findViewById(R.id.tv_remaining_balance)
        val tvMonthlyPayment: TextView = itemView.findViewById(R.id.tv_monthly_payment)
        val tvInterestRate: TextView = itemView.findViewById(R.id.tv_interest_rate)
        val tvTotalRepayment: TextView = itemView.findViewById(R.id.tv_total_repayment)
        val tvTotalInterest: TextView = itemView.findViewById(R.id.tv_total_interest)
        val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < loans.size) VIEW_TYPE_LOAN else VIEW_TYPE_CREDIT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOAN -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loan_credit_detailed, parent, false)
                LoanViewHolder(view)
            }
            VIEW_TYPE_CREDIT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loan_credit_detailed, parent, false)
                CreditViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LoanViewHolder -> bindLoanViewHolder(holder, position)
            is CreditViewHolder -> bindCreditViewHolder(holder, position - loans.size)
        }
    }

    private fun bindLoanViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        val context = holder.itemView.context

        // Set name and type
        holder.tvName.text = loan.name
        holder.tvType.text = "Laina"
        holder.tvAmount.text = String.format("€%.2f", loan.remainingBalance)

        // Calculate progress
        val progress = ((loan.originalAmount - loan.remainingBalance) / loan.originalAmount * 100).toInt()
        holder.tvProgressPercentage.text = "$progress%"

        // Set details
        holder.tvRemainingBalance.text = String.format("€%.2f", loan.remainingBalance)
        holder.tvMonthlyPayment.text = String.format("€%.2f", loan.monthlyPaymentAmount)
        holder.tvInterestRate.text = String.format("%.2f%%", loan.interestRate)
        holder.tvTotalRepayment.text = String.format("€%.2f", loan.totalRepaymentAmount)
        holder.tvTotalInterest.text = String.format("€%.2f", loan.totalInterestAmount)
        
        // Format due date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.tvDueDate.text = dateFormat.format(loan.dueDate)

        // Set button listeners
        holder.btnEdit.setOnClickListener { onEditLoan(loan) }
        holder.btnDelete.setOnClickListener { onDeleteLoan(loan) }
    }

    private fun bindCreditViewHolder(holder: CreditViewHolder, position: Int) {
        val credit = credits[position]
        val context = holder.itemView.context

        // Set name and type
        holder.tvName.text = credit.name
        holder.tvType.text = "Luotto"
        holder.tvAmount.text = String.format("€%.2f", credit.currentBalance)

        // Calculate progress
        val progress = ((credit.creditLimit - credit.currentBalance) / credit.creditLimit * 100).toInt()
        holder.tvProgressPercentage.text = "$progress%"

        // Set details
        holder.tvRemainingBalance.text = String.format("€%.2f", credit.currentBalance)
        holder.tvMonthlyPayment.text = String.format("€%.2f", credit.minimumPaymentAmount)
        holder.tvInterestRate.text = String.format("%.2f%%", credit.interestRate)
        holder.tvTotalRepayment.text = String.format("€%.2f", credit.totalRepaymentAmount)
        holder.tvTotalInterest.text = String.format("€%.2f", credit.totalInterestAmount)
        
        // Format due date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.tvDueDate.text = dateFormat.format(credit.dueDate)

        // Set button listeners
        holder.btnEdit.setOnClickListener { onEditCredit(credit) }
        holder.btnDelete.setOnClickListener { onDeleteCredit(credit) }
    }

    override fun getItemCount(): Int = loans.size + credits.size

    fun updateData(newLoans: List<Loan>, newCredits: List<Credit>) {
        loans = newLoans
        credits = newCredits
        notifyDataSetChanged()
    }
} 