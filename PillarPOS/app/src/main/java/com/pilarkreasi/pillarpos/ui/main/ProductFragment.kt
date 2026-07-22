package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.Product
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentProdukBinding
import com.pilarkreasi.pillarpos.util.SessionManager
import java.text.NumberFormat
import java.util.Locale

class ProductFragment : Fragment() {

    private var _binding: FragmentProdukBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    
    private val cartViewModel: CartViewModel by activityViewModels()

    
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
        _binding = FragmentProdukBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRoleBasedUI()
        setupCategories()
        setupSearch()
        setupProducts()
        observeCart()

        binding.btnCharge.setOnClickListener {
            if (cartViewModel.getCartCount() > 0) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, PaymentFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupRoleBasedUI() {
        
        
        
        val isAdmin = sessionManager.getRole() == Role.ADMIN
        binding.layoutCartSummary.visibility = if (isAdmin) View.GONE else View.VISIBLE
    }

    private fun setupCategories() {
        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val semua = com.pilarkreasi.pillarpos.data.model.Category(0, "Semua")
            val categoryList = listOf(semua) + categories.map {
                com.pilarkreasi.pillarpos.data.model.Category(it.idCategory, it.name)
            }
            val adapter = CategoryAdapter(categoryList) { category ->
                productViewModel.selectCategory(if (category.id == 0) null else category.id)
            }
            binding.rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvCategories.adapter = adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                productViewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupProducts() {
        val isAdmin = sessionManager.getRole() == Role.ADMIN
        productViewModel.products.observe(viewLifecycleOwner) { productEntities ->
            val products = productEntities.map { entity ->
                Product(entity.idProduct, entity.name, entity.price, entity.idCategory ?: 0, entity.imageUrl, entity.stock)
            }
            val adapter = ProductAdapter(products, isAdmin) { product ->
                if (isAdmin) {
                    android.widget.Toast.makeText(
                        context,
                        "${product.name} - Stok: ${product.stock}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    cartViewModel.addToCart(product)
                }
            }
            binding.rvProducts.layoutManager = GridLayoutManager(context, 2)
            binding.rvProducts.adapter = adapter
        }
    }

    

    private fun observeCart() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        binding.rvCartLines.layoutManager = LinearLayoutManager(context)

        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            val count = items.sumOf { it.quantity }
            binding.tvItemCount.text = getString(R.string.item_count_format, count)

            binding.rvCartLines.adapter = CartLineAdapter(
                items = items,
                onIncrease = { item -> cartViewModel.addToCart(item.product) },
                onDecrease = { item -> cartViewModel.removeFromCart(item.product) }
            )
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            binding.tvTotalPrice.text = formatter.format(total).replace("Rp", "Rp ")
            binding.btnCharge.isEnabled = total > 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}