package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Transaction
import java.text.SimpleDateFormat
import java.util.*

class DetailedPaymentAdapter(
    private var payments: List<Transaction>,
    private val onPaymentStatusChanged: (Transaction, Boolean) -> Unit,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<DetailedPaymentAdapter.PaymentViewHolder>() {

    private var showAllItems = false
    private val maxVisibleItems = 2

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbPaymentStatus: CheckBox = itemView.findViewById(R.id.cb_payment_status)
        val tvPaymentName: TextView = itemView.findViewById(R.id.tv_payment_name)
        val tvPaymentAmount: TextView = itemView.findViewById(R.id.tv_payment_amount)
        val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        val tvCountdown: TextView = itemView.findViewById(R.id.tv_countdown)
        val tvPaymentTypeBadge: TextView = itemView.findViewById(R.id.tv_payment_type_badge)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_detailed, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        val context = holder.itemView.context

        // Payment name and amount
        holder.tvPaymentName.text = payment.name
        holder.tvPaymentAmount.text = String.format(Locale.getDefault(), "%.2f €", payment.amount)

        // Due date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        holder.tvDueDate.text = "Due: ${dateFormat.format(payment.dueDate)}"

        // Countdown and color coding
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val dueDate = Calendar.getInstance().apply {
            time = payment.dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val daysDiff = ((dueDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

        when {
            daysDiff < 0 -> {
                // Overdue
                holder.tvCountdown.text = context.getString(R.string.overdue_days, Math.abs(daysDiff))
                holder.tvCountdown.setTextColor(context.getColor(R.color.accent_red))
                holder.tvStatusBadge.text = context.getString(R.string.overdue)
                holder.tvStatusBadge.setTextColor(context.getColor(R.color.accent_red))
            }
            daysDiff == 0 -> {
                // Due today
                holder.tvCountdown.text = context.getString(R.string.due_today)
                holder.tvCountdown.setTextColor(context.getColor(R.color.accent_red))
                holder.tvStatusBadge.text = context.getString(R.string.due_today)
                holder.tvStatusBadge.setTextColor(context.getColor(R.color.accent_red))
            }
            daysDiff <= 7 -> {
                // Due within 7 days
                holder.tvCountdown.text = context.getString(R.string.due_in_days, daysDiff)
                holder.tvCountdown.setTextColor(context.getColor(R.color.orange))
                holder.tvStatusBadge.text = context.getString(R.string.unpaid)
                holder.tvStatusBadge.setTextColor(context.getColor(R.color.orange))
            }
            else -> {
                // Normal
                holder.tvCountdown.text = context.getString(R.string.due_in_days, daysDiff)
                holder.tvCountdown.setTextColor(context.getColor(R.color.text_primary))
                holder.tvStatusBadge.text = context.getString(R.string.unpaid)
                holder.tvStatusBadge.setTextColor(context.getColor(R.color.text_primary))
            }
        }

        // Payment type badge
        when {
            payment.isLoanRepayment -> {
                holder.tvPaymentTypeBadge.text = context.getString(R.string.loan)
                holder.tvPaymentTypeBadge.visibility = View.VISIBLE
            }
            payment.isCreditRepayment -> {
                holder.tvPaymentTypeBadge.text = context.getString(R.string.credit)
                holder.tvPaymentTypeBadge.visibility = View.VISIBLE
            }
            else -> {
                holder.tvPaymentTypeBadge.visibility = View.GONE
            }
        }

        // Payment status
        holder.cbPaymentStatus.isChecked = payment.isPaid
        holder.cbPaymentStatus.setOnCheckedChangeListener { _, isChecked ->
            onPaymentStatusChanged(payment, isChecked)
        }

        // Long click for edit/delete options
        holder.itemView.setOnLongClickListener {
            showEditDeleteDialog(payment, context)
            true
        }
    }

    override fun getItemCount(): Int {
        return if (showAllItems) payments.size else minOf(payments.size, maxVisibleItems)
    }

    fun updatePayments(newPayments: List<Transaction>) {
        // Smart sorting: unpaid first, then by due date
        payments = newPayments.sortedWith(compareBy<Transaction> { it.isPaid }
            .thenBy { it.dueDate })
        notifyDataSetChanged()
    }

    fun showAllItems() {
        showAllItems = true
        notifyDataSetChanged()
    }

    fun showFirstItems(count: Int) {
        showAllItems = false
        notifyDataSetChanged()
    }

    private fun showEditDeleteDialog(payment: Transaction, context: android.content.Context) {
        val options = arrayOf("Edit", "Delete")
        android.app.AlertDialog.Builder(context)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onEditClick(payment)
                    1 -> onDeleteClick(payment)
                }
            }
            .show()
    }
} 