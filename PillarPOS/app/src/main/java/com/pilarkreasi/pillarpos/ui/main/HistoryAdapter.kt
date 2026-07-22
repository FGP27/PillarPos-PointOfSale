package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val transactions: List<TransactionEntity>,
    private val onItemClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class HistoryViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionEntity) {
            binding.tvTransactionId.text = "#TRX-${item.idTransaction}"
            
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            binding.tvDate.text = sdf.format(item.transactionDate)
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvTotalAmount.text = formatter.format(item.totalAmount).replace("Rp", "Rp ")
            
            binding.tvPaymentType.text = item.paymentType

            val statusLabel = when (item.paymentStatus) {
                "PENDING_VOID" -> "Menunggu Persetujuan Void"
                "VOID" -> "Dibatalkan (Void)"
                else -> "Selesai"
            }
            binding.tvStatus.text = statusLabel

            when (item.paymentStatus) {
                "VOID" -> {
                    binding.tvStatus.alpha = 0.5f
                    binding.tvVoidReason.visibility = View.VISIBLE
                    binding.tvVoidReason.text = "Alasan: ${item.voidReason}"
                    binding.root.alpha = 0.7f
                }
                "PENDING_VOID" -> {
                    binding.tvStatus.alpha = 1.0f
                    binding.tvVoidReason.visibility = View.VISIBLE
                    binding.tvVoidReason.text = "Alasan pengajuan: ${item.voidReason}"
                    binding.root.alpha = 1.0f
                }
                else -> {
                    binding.tvStatus.alpha = 1.0f
                    binding.tvVoidReason.visibility = View.GONE
                    binding.root.alpha = 1.0f
                }
            }
            
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
