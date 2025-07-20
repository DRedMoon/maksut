package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Credit
import java.text.SimpleDateFormat
import java.util.*

class CreditAdapter(
    private val onEditClick: (Credit) -> Unit,
    private val onDeleteClick: (Credit) -> Unit
) : RecyclerView.Adapter<CreditAdapter.CreditViewHolder>() {
    
    private var credits: List<Credit> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun updateCredits(newCredits: List<Credit>) {
        credits = newCredits
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credit, parent, false)
        return CreditViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(credits[position])
    }
    
    override fun getItemCount(): Int = credits.size
    
    inner class CreditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_credit_name)
        private val tvLimit: TextView = itemView.findViewById(R.id.tv_credit_limit)
        private val tvBalance: TextView = itemView.findViewById(R.id.tv_credit_balance)
        private val tvInterest: TextView = itemView.findViewById(R.id.tv_credit_interest)
        private val tvMinPayment: TextView = itemView.findViewById(R.id.tv_credit_min_payment)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_credit_due_date)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_credit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_credit)
        
        fun bind(credit: Credit) {
            tvName.text = credit.name
            tvLimit.text = String.format(Locale.getDefault(), "Raja: %.2f €", credit.creditLimit)
            tvBalance.text = String.format(Locale.getDefault(), "Saldo: %.2f €", credit.currentBalance)
            tvInterest.text = String.format(Locale.getDefault(), "%.2f%%", credit.totalInterestRate)
            tvMinPayment.text = String.format(Locale.getDefault(), "Min: %.2f €", credit.minimumPaymentAmount)
            tvDueDate.text = "Eräpäivä: ${credit.dueDay}. päivä"
            
            btnEdit.setOnClickListener { onEditClick(credit) }
            btnDelete.setOnClickListener { onDeleteClick(credit) }
        }
    }
}