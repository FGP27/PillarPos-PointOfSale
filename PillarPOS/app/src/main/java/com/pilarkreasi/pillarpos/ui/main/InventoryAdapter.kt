package com.pilarkreasi.pillarpos.ui.main

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.databinding.ItemInventoryBinding

class InventoryAdapter(
    private val products: List<ProductEntity>,
    private val isAdmin: Boolean,
    private val onUpdateClick: (ProductEntity) -> Unit,
    private val onMoreClick: (ProductEntity, android.view.View) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class InventoryViewHolder(private val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductEntity) {
            binding.tvProductName.text = item.name
            binding.tvCurrentStock.text = "Stok saat ini: ${item.stock}"

            val imageUrl = item.imageUrl
            when {
                imageUrl.isNullOrEmpty() -> {
                    binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                }
                imageUrl.startsWith("drawable://") -> {
                    val resName = imageUrl.removePrefix("drawable://")
                    val context = binding.root.context
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    binding.ivProduct.setImageResource(
                        if (resId != 0) resId else android.R.drawable.ic_menu_gallery
                    )
                }
                else -> {
                    try {
                        binding.ivProduct.setImageURI(Uri.parse(imageUrl))
                    } catch (e: Exception) {
                        binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }

            binding.btnUpdateStock.setOnClickListener { onUpdateClick(item) }

            
            binding.btnMore.visibility = if (isAdmin) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnMore.setOnClickListener { onMoreClick(item, it) }
        }
    }
}