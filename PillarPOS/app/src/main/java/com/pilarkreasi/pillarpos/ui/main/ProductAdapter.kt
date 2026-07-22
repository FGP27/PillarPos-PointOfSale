package com.pilarkreasi.pillarpos.ui.main

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.Product
import com.pilarkreasi.pillarpos.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: List<Product>,
    private val isAdmin: Boolean,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvProductPrice.text = formatter.format(product.price).replace("Rp", "Rp ")

            
            
            
            val imageUrl = product.imageUrl
            when {
                imageUrl.isNullOrEmpty() -> {
                    binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                }
                imageUrl.startsWith("drawable://") -> {
                    val resName = imageUrl.removePrefix("drawable://")
                    val context = binding.root.context
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) {
                        binding.ivProduct.setImageResource(resId)
                    } else {
                        binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
                else -> {
                    try {
                        binding.ivProduct.setImageURI(Uri.parse(imageUrl))
                    } catch (e: Exception) {
                        binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }

            binding.root.setOnClickListener { onItemClick(product) }

            if (isAdmin) {
                
                binding.btnAdd.visibility = View.GONE
            } else {
                binding.btnAdd.visibility = View.VISIBLE
                binding.btnAdd.setOnClickListener { onItemClick(product) }
            }
        }
    }
}