package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.model.Category
import com.pilarkreasi.pillarpos.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category, position: Int) {
            binding.btnCategory.text = category.name
            
            val isSelected = selectedPosition == position
            if (isSelected) {
                binding.btnCategory.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary))
                binding.btnCategory.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
            } else {
                binding.btnCategory.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.btnCategory.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary))
            }

            binding.btnCategory.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size
}
