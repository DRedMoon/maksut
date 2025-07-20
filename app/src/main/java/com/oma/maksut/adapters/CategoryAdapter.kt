package com.oma.maksut.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oma.maksut.R
import com.oma.maksut.database.entities.Category

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    private var categories: List<Category> = emptyList()
    
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }
    
    override fun getItemCount(): Int = categories.size
    
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvOptions: TextView = itemView.findViewById(R.id.tv_category_options)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_category)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_category)
        
        fun bind(category: Category) {
            tvName.text = category.name
            
            val options = mutableListOf<String>()
            if (category.hasDueDate) options.add("Due Date")
            if (category.isMonthlyPayment) options.add("Monthly Payment")
            
            tvOptions.text = if (options.isNotEmpty()) {
                options.joinToString(", ")
            } else {
                "No special options"
            }
            
            btnEdit.setOnClickListener { onEditClick(category) }
            btnDelete.setOnClickListener { onDeleteClick(category) }
        }
    }
}