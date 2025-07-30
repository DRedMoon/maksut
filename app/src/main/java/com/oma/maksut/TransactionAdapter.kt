package com.oma.maksut

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import java.util.Locale
import com.oma.maksut.database.entities.Transaction
import java.text.SimpleDateFormat

class TransactionAdapter(
    private var items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_transaction_icon)
        val name: TextView = view.findViewById(R.id.tv_transaction_name)
        val date: TextView = view.findViewById(R.id.tv_transaction_date)
        val category: TextView = view.findViewById(R.id.tv_transaction_category)
        val amount: TextView = view.findViewById(R.id.tv_transaction_amount)
        val label: TextView = view.findViewById(R.id.tv_transaction_label)
        val menuButton: ImageButton = view.findViewById(R.id.btn_transaction_menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = items[position]
        
        // Set transaction name
        holder.name.text = tx.name

        // Set amount with color
        holder.amount.text = String.format(Locale.getDefault(), "€%.2f", tx.amount)
        holder.amount.setTextColor(if (tx.amount >= 0) Color.GREEN else Color.RED)

        // Set date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.date.text = dateFormat.format(tx.paymentDate)

        // Set category
        val categoryText = if (tx.isLoanRepayment) "Loan" else if (tx.isCreditRepayment) "Credit" else "Transaction"
        holder.category.text = categoryText

        // Set label (Income/Expense)
        val labelText = if (tx.amount >= 0) "Income" else "Expense"
        holder.label.text = labelText

        // Set icon based on transaction type
        when {
            tx.isLoanRepayment -> holder.icon.setImageResource(R.drawable.ic_edit) // Use loan icon
            tx.isCreditRepayment -> holder.icon.setImageResource(R.drawable.ic_edit) // Use credit icon
            else -> holder.icon.setImageResource(R.drawable.ic_analytics) // Use default transaction icon
        }

        // Set up menu button
        holder.menuButton.setOnClickListener {
            // TODO: Show popup menu for edit/delete options
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Transaction>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                return items[oldPos].id == newItems[newPos].id
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                return items[oldPos] == newItems[newPos]
            }
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    fun updateTransactions(newItems: List<Transaction>) {
        updateItems(newItems)
    }
}
