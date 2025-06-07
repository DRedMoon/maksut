// TransactionAdapter.kt
package com.oma.maksut

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TransactionAdapter(
    private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_tx_icon)
        val title: TextView = view.findViewById(R.id.tv_tx_title)
        val amount: TextView = view.findViewById(R.id.tv_tx_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tx = items[position]
        holder.icon.setImageResource(tx.iconRes)
        holder.title.text = tx.title

        // Muutetaan Double merkkijonoksi ja värjätään
        holder.amount.text = String.format(
            Locale.getDefault(),
            "%+.2f €", tx.amount)
        holder.amount.setTextColor(
            if (tx.amount >= 0) Color.GREEN else Color.RED
        )
    }

    override fun getItemCount() = items.size
}
