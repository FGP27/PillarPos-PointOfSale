package com.pilarkreasi.pillarpos.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentInventoryBinding
import com.pilarkreasi.pillarpos.util.SessionManager

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
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
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        val isAdmin = sessionManager.getRole() == Role.ADMIN
        binding.fabAddProduct.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        
        
        val isAdmin = sessionManager.getRole() == Role.ADMIN

        productViewModel.allProductsForInventory.observe(viewLifecycleOwner) { products ->
            val adapter = InventoryAdapter(
                products = products,
                isAdmin = isAdmin,
                onUpdateClick = { product ->
                    if (isAdmin) {
                        showUpdateStockDialog(product)
                    } else {
                        Toast.makeText(context, "Kasir hanya dapat melihat stok", Toast.LENGTH_SHORT).show()
                    }
                },
                onMoreClick = { product, anchorView ->
                    showProductMenu(product, anchorView)
                }
            )
            binding.rvInventory.adapter = adapter
        }
    }

    

    private fun showProductMenu(product: ProductEntity, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, MENU_EDIT, 0, "Ubah Produk")
        popup.menu.add(0, MENU_DELETE, 1, "Hapus Produk")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_EDIT -> {
                    val intent = Intent(requireContext(), AddProductActivity::class.java)
                    intent.putExtra(AddProductActivity.EXTRA_PRODUCT_ID, product.idProduct)
                    startActivity(intent)
                    true
                }
                MENU_DELETE -> {
                    confirmDeleteProduct(product)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmDeleteProduct(product: ProductEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus \"${product.name}\"? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                productViewModel.deleteProduct(
                    product = product,
                    onDone = {
                        Toast.makeText(context, "Produk \"${product.name}\" berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        
                        
                        Toast.makeText(
                            context,
                            "Produk \"${product.name}\" tidak dapat dihapus karena masih memiliki riwayat transaksi",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showUpdateStockDialog(product: ProductEntity) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Stok: ${product.name}")

        val input = EditText(requireContext())
        input.hint = "Masukkan jumlah stok baru"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setPadding(48, 20, 48, 20)
        builder.setView(input)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val newStockStr = input.text.toString()
            if (newStockStr.isNotEmpty()) {
                val newStock = newStockStr.toInt()
                productViewModel.updateProductStock(product.idProduct, newStock)
                Toast.makeText(context, "Stok berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MENU_EDIT = 1
        private const val MENU_DELETE = 2
    }
}