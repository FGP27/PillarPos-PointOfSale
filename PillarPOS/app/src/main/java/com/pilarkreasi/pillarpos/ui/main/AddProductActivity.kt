package com.pilarkreasi.pillarpos.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.ActivityAddProductBinding


class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private var selectedImageUri: Uri? = null

    
    private var editingProductId: Int = -1
    private var existingImageUrl: String? = null
    private var pendingCategoryId: Int? = null

    private val viewModel: ProductViewModel by viewModels {
        val database = PillarPosDatabase.getInstance(this)
        ProductViewModelFactory(ProductRepository.create(this, database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao()))
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivProduct.setImageURI(selectedImageUri)
            binding.ivProduct.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

            
            selectedImageUri?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingProductId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)

        setupDropdown()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            imagePickerLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        if (editingProductId != -1) {
            binding.tvTitle.text = "Ubah Produk"
            binding.btnSave.text = "SIMPAN PERUBAHAN"
            loadProductForEdit(editingProductId)
        }
    }

    private fun loadProductForEdit(productId: Int) {
        viewModel.loadProductById(productId) { product ->
            if (product == null) {
                Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
                return@loadProductById
            }
            binding.etProductName.setText(product.name)
            binding.etPrice.setText(product.price.toInt().toString())
            binding.etStock.setText(product.stock.toString())
            existingImageUrl = product.imageUrl
            pendingCategoryId = product.idCategory

            
            applyPendingCategoryIfReady()

            existingImageUrl?.let { imageUrl -> showExistingImage(imageUrl) }
        }
    }

    private fun showExistingImage(imageUrl: String) {
        when {
            imageUrl.isEmpty() -> Unit
            imageUrl.startsWith("drawable://") -> {
                val resName = imageUrl.removePrefix("drawable://")
                val resId = resources.getIdentifier(resName, "drawable", packageName)
                if (resId != 0) {
                    binding.ivProduct.setImageResource(resId)
                    binding.ivProduct.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                }
            }
            else -> {
                try {
                    binding.ivProduct.setImageURI(Uri.parse(imageUrl))
                    binding.ivProduct.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                } catch (e: Exception) {
                    
                }
            }
        }
    }

    private fun applyPendingCategoryIfReady() {
        val categoryId = pendingCategoryId ?: return
        val categoryName = viewModel.categories.value?.find { it.idCategory == categoryId }?.name ?: return
        binding.spinnerCategory.setText(categoryName, false)
    }

    private fun setupDropdown() {
        viewModel.categories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }.toMutableList()
            
            categoryNames.add("+ Tambah Kategori Baru")

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
            binding.spinnerCategory.setAdapter(adapter)

            binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
                if (categoryNames[position] == "+ Tambah Kategori Baru") {
                    showAddCategoryDialog()
                }
            }

            
            applyPendingCategoryIfReady()
        }
    }

    private fun showAddCategoryDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Tambah Kategori")

        val input = android.widget.EditText(this)
        input.hint = "Nama Kategori Baru"
        builder.setView(input)

        builder.setPositiveButton("Tambah") { dialog, _ ->
            val categoryName = input.text.toString()
            if (categoryName.isNotEmpty()) {
                viewModel.addCategory(CategoryEntity(name = categoryName))
                Toast.makeText(this, "Kategori $categoryName ditambahkan", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString()
        val priceString = binding.etPrice.text.toString()
        val stockString = binding.etStock.text.toString()
        val categoryName = binding.spinnerCategory.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama produk wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceString.toDoubleOrNull() ?: 0.0
        if (price <= 0) {
            Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val stock = stockString.toIntOrNull() ?: 0

        val category = viewModel.categories.value?.find { it.name == categoryName }
        if (category == null) {
            Toast.makeText(this, "Silakan pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }

        
        val imageUrl = selectedImageUri?.toString() ?: existingImageUrl

        if (editingProductId != -1) {
            val product = ProductEntity(
                idProduct = editingProductId,
                name = name,
                price = price,
                stock = stock,
                idCategory = category.idCategory,
                imageUrl = imageUrl
            )
            viewModel.updateProduct(product) {
                runOnUiThread {
                    Toast.makeText(this, "Produk $name berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            val product = ProductEntity(
                name = name,
                price = price,
                stock = stock,
                idCategory = category.idCategory,
                imageUrl = imageUrl
            )
            viewModel.addProduct(product) {
                runOnUiThread {
                    Toast.makeText(this, "Produk $name berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }
}