package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.DiscountType
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.databinding.ItemDiscountBinding

class DiscountAdapter(
    private val discounts: List<DiscountEntity>,
    private val products: List<ProductEntity>,
    private val categories: List<CategoryEntity>,
    private val onEditClick: (DiscountEntity) -> Unit,
    private val onDeleteClick: (DiscountEntity) -> Unit
) : RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = ItemDiscountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(discounts[position])
    }

    override fun getItemCount(): Int = discounts.size

    inner class DiscountViewHolder(private val binding: ItemDiscountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscountEntity) {
            binding.tvDiscountName.text = item.name

            val targetLabel = when (item.type) {
                DiscountType.PRODUCT -> {
                    val productName = products.find { it.idProduct == item.targetId }?.name ?: "Produk terhapus"
                    "Produk: $productName"
                }
                DiscountType.CATEGORY -> {
                    val categoryName = categories.find { it.idCategory == item.targetId }?.name ?: "Kategori terhapus"
                    "Kategori: $categoryName"
                }
                DiscountType.TOTAL -> "Total Transaksi"
            }
            binding.tvDiscountDetail.text = "$targetLabel - ${formatPercentage(item.percentage)}%"

            if (item.isActive) {
                binding.tvDiscountStatus.text = "AKTIF"
                binding.tvDiscountStatus.setTextColor(
                    binding.root.context.getColor(com.pilarkreasi.pillarpos.R.color.primary)
                )
            } else {
                binding.tvDiscountStatus.text = "NONAKTIF"
                binding.tvDiscountStatus.setTextColor(
                    binding.root.context.getColor(com.pilarkreasi.pillarpos.R.color.text_secondary)
                )
            }

            binding.btnEditDiscount.setOnClickListener { onEditClick(item) }
            binding.btnDeleteDiscount.setOnClickListener { onDeleteClick(item) }
        }

        private fun formatPercentage(value: Double): String {
            return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        }
    }
}