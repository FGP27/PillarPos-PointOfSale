package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.TopProduct
import com.pilarkreasi.pillarpos.databinding.ItemTopProductBinding

class TopProductAdapter(
    private val items: List<TopProduct>
) : RecyclerView.Adapter<TopProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemTopProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopProduct, rank: Int) {
            binding.tvRank.text = "$rank."
            binding.tvProductName.text = item.name
            binding.tvTotalSold.text = "${item.totalQty} terjual"
        }
    }
}
