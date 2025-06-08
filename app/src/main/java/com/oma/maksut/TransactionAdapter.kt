package com.oma.maksut

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapteri RecyclerView’lle, näyttää listan Transaction-olioita
class TransactionAdapter(
    private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // Holder-luokka item_transaction.xml:n näkymille
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView       = view.findViewById(R.id.iv_icon)
        val label: TextView       = view.findViewById(R.id.tv_label)
        val amount: TextView      = view.findViewById(R.id.tv_amount)
        val time: TextView        = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ladataan item_transaction-layout
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = items[position]
        holder.icon.setImageResource(tx.iconRes)
        holder.label.text = tx.label

        // Värjää summa riippuen sen merkistä
        holder.amount.text = String.format("%.2f €", tx.amount)
        if (tx.amount >= 0) {
            holder.amount.setTextColor(android.graphics.Color.GREEN)
        } else {
            holder.amount.setTextColor(android.graphics.Color.RED)
        }

        // Nyt näytetään päivämäärä, ei kellonaika
        holder.time.text = tx.time  // tx.time sisältää nyt esim. "2025-06-08"
    }

    override fun getItemCount(): Int = items.size
}
