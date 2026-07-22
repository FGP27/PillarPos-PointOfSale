package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.CartItem
import com.pilarkreasi.pillarpos.databinding.ItemCartLineBinding
import java.text.NumberFormat
import java.util.Locale


class CartLineAdapter(
    private val items: List<CartItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : RecyclerView.Adapter<CartLineAdapter.CartLineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartLineViewHolder {
        val binding = ItemCartLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartLineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartLineViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartLineViewHolder(private val binding: ItemCartLineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvCartLineName.text = item.product.name
            binding.tvCartLinePrice.text = formatter.format(item.product.price).replace("Rp", "Rp ")
            binding.tvCartLineQty.text = item.quantity.toString()
            binding.tvCartLineSubtotal.text = formatter.format(item.totalPrice).replace("Rp", "Rp ")

            binding.btnCartLineIncrease.setOnClickListener { onIncrease(item) }
            binding.btnCartLineDecrease.setOnClickListener { onDecrease(item) }
        }
    }
}