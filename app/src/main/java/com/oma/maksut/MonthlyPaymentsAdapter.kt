package com.oma.maksut

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MonthlyPaymentsAdapter(
    private var items: List<Transaction>,
    private val onStatusToggle: (Transaction) -> Unit
) : RecyclerView.Adapter<MonthlyPaymentsAdapter.ViewHolder>() {

    private var visibleCount = 10

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_payment_name)
        val amount: TextView = view.findViewById(R.id.tv_payment_amount)
        val due: TextView = view.findViewById(R.id.tv_payment_due)
        val status: ImageView = view.findViewById(R.id.iv_payment_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_payment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = items[position]
        holder.name.text = tx.label
        holder.amount.text = String.format(Locale.getDefault(), "%.2f €", tx.amount)
        holder.due.text = tx.dueDate ?: ""
        val paid = tx.isPaid
        holder.status.setImageResource(
            if (paid) R.drawable.ic_check_circle else R.drawable.ic_radio_button_unchecked
        )
        holder.status.setOnClickListener { onStatusToggle(tx) }
    }

    override fun getItemCount(): Int = minOf(items.size, visibleCount)

    fun showMore() {
        visibleCount += 10
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<Transaction>) {
        items = newItems
        notifyDataSetChanged()
    }
}