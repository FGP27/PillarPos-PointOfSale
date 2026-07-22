package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.databinding.ItemVoidApprovalBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class VoidApprovalAdapter(
    private val transactions: List<TransactionEntity>,
    private val onApprove: (TransactionEntity) -> Unit,
    private val onReject: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<VoidApprovalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVoidApprovalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class ViewHolder(private val binding: ItemVoidApprovalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionEntity) {
            binding.tvTransactionId.text = "#TRX-${item.idTransaction}"

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            binding.tvDate.text = sdf.format(item.transactionDate)

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvTotalAmount.text = formatter.format(item.totalAmount).replace("Rp", "Rp ")

            binding.tvVoidReason.text = item.voidReason ?: "-"

            binding.btnApprove.setOnClickListener { onApprove(item) }
            binding.btnReject.setOnClickListener { onReject(item) }
        }
    }
}
