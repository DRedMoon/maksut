package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Loan
import java.text.SimpleDateFormat
import java.util.*

class LoanAdapter(
    private val onEditClick: (Loan) -> Unit,
    private val onDeleteClick: (Loan) -> Unit
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {
    
    private var loans: List<Loan> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun updateLoans(newLoans: List<Loan>) {
        loans = newLoans
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        holder.bind(loans[position])
    }
    
    override fun getItemCount(): Int = loans.size
    
    inner class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_loan_name)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_loan_amount)
        private val tvInterest: TextView = itemView.findViewById(R.id.tv_loan_interest)
        private val tvMonthlyPayment: TextView = itemView.findViewById(R.id.tv_loan_monthly)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_loan_due_date)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_loan)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_loan)
        
        fun bind(loan: Loan) {
            tvName.text = loan.name
            tvAmount.text = String.format(Locale.getDefault(), "%.2f €", loan.currentBalance)
            tvInterest.text = String.format(Locale.getDefault(), "%.2f%%", loan.totalInterestRate)
            tvMonthlyPayment.text = String.format(Locale.getDefault(), "%.2f €/kk", loan.monthlyPayment)
            tvDueDate.text = "Eräpäivä: ${loan.dueDay}. päivä"
            
            btnEdit.setOnClickListener { onEditClick(loan) }
            btnDelete.setOnClickListener { onDeleteClick(loan) }
        }
    }
}