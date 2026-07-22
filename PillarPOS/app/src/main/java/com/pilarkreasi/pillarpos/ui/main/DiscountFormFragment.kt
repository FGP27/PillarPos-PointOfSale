package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.DiscountType
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentDiscountFormBinding


class DiscountFormFragment : Fragment() {

    private var _binding: FragmentDiscountFormBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        ProductViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
        )
    }

    private var editingDiscountId: Int = -1
    private var didPrefill = false

    private var currentProducts: List<ProductEntity> = emptyList()
    private var currentCategories: List<CategoryEntity> = emptyList()

    private val typeLabels = listOf("Per Produk", "Per Kategori", "Total Transaksi")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscountFormBinding.inflate(inflater, container, false)
        editingDiscountId = arguments?.getInt(ARG_DISCOUNT_ID, -1) ?: -1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingDiscountId != -1) {
            binding.tvFormTitle.text = "Ubah Diskon"
            binding.btnSaveDiscount.text = "SIMPAN PERUBAHAN"
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupTypeDropdown()
        setupProductAndCategoryDropdowns()
        binding.btnSaveDiscount.setOnClickListener { saveDiscount() }
    }

    private fun setupTypeDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeLabels)
        binding.spinnerDiscountType.setAdapter(adapter)
        binding.spinnerDiscountType.setOnItemClickListener { _, _, position, _ ->
            updateTargetFieldVisibility(typeLabels[position])
        }
        
        if (binding.spinnerDiscountType.text.isNullOrEmpty()) {
            binding.spinnerDiscountType.setText(typeLabels[0], false)
            updateTargetFieldVisibility(typeLabels[0])
        }
    }

    private fun updateTargetFieldVisibility(selectedLabel: String) {
        binding.layoutTargetProduct.visibility = if (selectedLabel == "Per Produk") View.VISIBLE else View.GONE
        binding.layoutTargetCategory.visibility = if (selectedLabel == "Per Kategori") View.VISIBLE else View.GONE
    }

    private fun setupProductAndCategoryDropdowns() {
        productViewModel.allProductsForInventory.observe(viewLifecycleOwner) { products ->
            currentProducts = products
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, products.map { it.name })
            binding.spinnerTargetProduct.setAdapter(adapter)
            prefillIfReady()
        }

        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories.map { it.name })
            binding.spinnerTargetCategory.setAdapter(adapter)
            prefillIfReady()
        }

        if (editingDiscountId != -1) {
            productViewModel.allDiscounts.observe(viewLifecycleOwner) { discounts ->
                if (!didPrefill) {
                    discounts.find { it.idDiscount == editingDiscountId }?.let { prefillForm(it) }
                }
            }
        }
    }

    private var pendingDiscount: DiscountEntity? = null

    private fun prefillForm(discount: DiscountEntity) {
        pendingDiscount = discount
        binding.etDiscountName.setText(discount.name)
        binding.etDiscountPercentage.setText(formatPercentageForInput(discount.percentage))
        binding.switchActive.isChecked = discount.isActive

        val label = when (discount.type) {
            DiscountType.PRODUCT -> typeLabels[0]
            DiscountType.CATEGORY -> typeLabels[1]
            DiscountType.TOTAL -> typeLabels[2]
        }
        binding.spinnerDiscountType.setText(label, false)
        updateTargetFieldVisibility(label)

        prefillIfReady()
    }

    private fun prefillIfReady() {
        val discount = pendingDiscount ?: return
        if (didPrefill) return
        when (discount.type) {
            DiscountType.PRODUCT -> {
                val productName = currentProducts.find { it.idProduct == discount.targetId }?.name
                if (productName != null) {
                    binding.spinnerTargetProduct.setText(productName, false)
                    didPrefill = true
                }
            }
            DiscountType.CATEGORY -> {
                val categoryName = currentCategories.find { it.idCategory == discount.targetId }?.name
                if (categoryName != null) {
                    binding.spinnerTargetCategory.setText(categoryName, false)
                    didPrefill = true
                }
            }
            DiscountType.TOTAL -> {
                didPrefill = true
            }
        }
    }

    private fun formatPercentageForInput(value: Double): String {
        return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    }

    private fun saveDiscount() {
        val name = binding.etDiscountName.text.toString().trim()
        val percentageText = binding.etDiscountPercentage.text.toString().trim()
        val selectedTypeLabel = binding.spinnerDiscountType.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(context, "Nama diskon wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val percentage = percentageText.toDoubleOrNull()
        if (percentage == null || percentage <= 0 || percentage > 100) {
            Toast.makeText(context, "Persentase diskon harus antara 1-100", Toast.LENGTH_SHORT).show()
            return
        }

        val type = when (selectedTypeLabel) {
            typeLabels[0] -> DiscountType.PRODUCT
            typeLabels[1] -> DiscountType.CATEGORY
            else -> DiscountType.TOTAL
        }

        val targetId: Int? = when (type) {
            DiscountType.PRODUCT -> {
                val productName = binding.spinnerTargetProduct.text.toString()
                val product = currentProducts.find { it.name == productName }
                if (product == null) {
                    Toast.makeText(context, "Silakan pilih produk", Toast.LENGTH_SHORT).show()
                    return
                }
                product.idProduct
            }
            DiscountType.CATEGORY -> {
                val categoryName = binding.spinnerTargetCategory.text.toString()
                val category = currentCategories.find { it.name == categoryName }
                if (category == null) {
                    Toast.makeText(context, "Silakan pilih kategori", Toast.LENGTH_SHORT).show()
                    return
                }
                category.idCategory
            }
            DiscountType.TOTAL -> null
        }

        val discount = DiscountEntity(
            idDiscount = if (editingDiscountId != -1) editingDiscountId else 0,
            name = name,
            type = type,
            targetId = targetId,
            percentage = percentage,
            isActive = binding.switchActive.isChecked
        )

        if (editingDiscountId != -1) {
            productViewModel.updateDiscount(discount)
            Toast.makeText(context, "Diskon berhasil diperbarui", Toast.LENGTH_SHORT).show()
        } else {
            productViewModel.addDiscount(discount)
            Toast.makeText(context, "Diskon berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_DISCOUNT_ID = "arg_discount_id"
    }
}