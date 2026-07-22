package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentVoidApprovalBinding


class VoidApprovalFragment : Fragment() {

    private var _binding: FragmentVoidApprovalBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        ProductViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoidApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        productViewModel.pendingVoidTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvVoidApproval.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvVoidApproval.visibility = View.VISIBLE

                binding.rvVoidApproval.adapter = VoidApprovalAdapter(
                    transactions,
                    onApprove = { transaction -> confirmApprove(transaction) },
                    onReject = { transaction -> confirmReject(transaction) }
                )
            }
        }
    }

    private fun confirmApprove(transaction: TransactionEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Setujui Void?")
            .setMessage("Transaksi #TRX-${transaction.idTransaction} akan dibatalkan dan stok produk terkait akan dikembalikan.")
            .setPositiveButton("Approve") { _, _ ->
                productViewModel.approveVoid(transaction)
                Toast.makeText(context, "Void disetujui", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmReject(transaction: TransactionEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Tolak Pengajuan Void?")
            .setMessage("Transaksi #TRX-${transaction.idTransaction} akan tetap berlaku (tidak jadi dibatalkan).")
            .setPositiveButton("Reject") { _, _ ->
                productViewModel.rejectVoid(transaction)
                Toast.makeText(context, "Pengajuan void ditolak", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
