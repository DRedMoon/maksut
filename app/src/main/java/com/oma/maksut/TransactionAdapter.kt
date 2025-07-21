package com.oma.maksut

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import java.util.Locale

// Adapteri RecyclerView’lle, näyttää listan Transaction-olioita
class TransactionAdapter(
    private var items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // Holder-luokka item_transaction.xml:n näkymille
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView       = view.findViewById(R.id.tv_transaction_label)
        val amount: TextView      = view.findViewById(R.id.tv_transaction_amount)
        val time: TextView        = view.findViewById(R.id.tv_transaction_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ladataan item_transaction-layout
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = items[position]
        holder.label.text = tx.label

        // Värjää summa riippuen sen merkistä
        holder.amount.text = String.format(Locale.getDefault(), "%.2f €", tx.amount)
        holder.amount.setTextColor(if (tx.amount >= 0) Color.GREEN else Color.RED)

        // Nyt näytetään päivämäärä, ei kellonaika
        holder.time.text = tx.time  // tx.time sisältää nyt esim. "2025-06-08"
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Transaction>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                // Jos Transactionilla on uniikki tunniste, vertaa sitä.
                // Esim jos Transaction-luokalla olisi id-kenttä:
                return items[oldPos].id == newItems[newPos].id
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                // Palauttaa true jos koko objekti on sama
                return items[oldPos] == newItems[newPos]
            }
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }
}
