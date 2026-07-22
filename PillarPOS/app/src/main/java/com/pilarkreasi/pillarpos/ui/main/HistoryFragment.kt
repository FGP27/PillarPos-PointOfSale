package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentHistoryBinding
import com.pilarkreasi.pillarpos.util.SessionManager

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        productViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE

                
                
                val adapter = HistoryAdapter(transactions) { transaction ->
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, TransactionDetailFragment.newInstance(transaction.idTransaction))
                        .addToBackStack(null)
                        .commit()
                }
                binding.rvHistory.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
