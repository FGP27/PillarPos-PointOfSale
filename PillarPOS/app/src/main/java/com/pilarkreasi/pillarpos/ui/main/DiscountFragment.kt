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
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentDiscountBinding


class DiscountFragment : Fragment() {

    private var _binding: FragmentDiscountBinding? = null
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
        _binding = FragmentDiscountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.fabAddDiscount.setOnClickListener {
            openForm(discountId = null)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        productViewModel.allDiscounts.observe(viewLifecycleOwner) { discounts ->
            binding.tvEmptyState.visibility = if (discounts.isEmpty()) View.VISIBLE else View.GONE

            val products = productViewModel.allProductsForInventory.value ?: emptyList()
            val categories = productViewModel.categories.value ?: emptyList()

            binding.rvDiscounts.adapter = DiscountAdapter(
                discounts = discounts,
                products = products,
                categories = categories,
                onEditClick = { discount -> openForm(discount.idDiscount) },
                onDeleteClick = { discount -> confirmDelete(discount) }
            )
        }
    }

    private fun openForm(discountId: Int?) {
        val fragment = DiscountFormFragment().apply {
            arguments = Bundle().apply {
                if (discountId != null) putInt(DiscountFormFragment.ARG_DISCOUNT_ID, discountId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(com.pilarkreasi.pillarpos.R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(discount: DiscountEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Diskon")
            .setMessage("Yakin ingin menghapus diskon \"${discount.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                productViewModel.deleteDiscount(discount)
                Toast.makeText(context, "Diskon \"${discount.name}\" berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}