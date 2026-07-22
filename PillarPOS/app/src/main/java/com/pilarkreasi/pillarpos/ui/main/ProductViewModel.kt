package com.pilarkreasi.pillarpos.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.TransactionDetailEntity
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    val allProductsForInventory = repository.allProducts.asLiveData()
    val categories = repository.allCategories.asLiveData()
    val allTransactions = repository.allTransactions.asLiveData()
    val pendingVoidTransactions = repository.pendingVoidTransactions.asLiveData()
    val todayTotalSales = repository.todayTotalSales.asLiveData()
    val todayTransactionCount = repository.todayTransactionCount.asLiveData()
    val allDiscounts = repository.allDiscounts.asLiveData()
    val activeDiscounts = repository.activeDiscounts.asLiveData()

    private val selectedCategoryId = MutableStateFlow<Int?>(null)
    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val productsByCategory = selectedCategoryId.flatMapLatest { id ->
        if (id == null || id == 0) {
            repository.allProducts
        } else {
            repository.getProductsByCategory(id)
        }
    }

    

    val products = kotlinx.coroutines.flow.combine(productsByCategory, searchQuery) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.asLiveData()

    fun selectCategory(categoryId: Int?) {
        selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    

    fun addProduct(product: com.pilarkreasi.pillarpos.data.model.ProductEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertProduct(product)
            onDone()
        }
    }

    

    fun loadProductById(productId: Int, onLoaded: (com.pilarkreasi.pillarpos.data.model.ProductEntity?) -> Unit) {
        viewModelScope.launch {
            onLoaded(repository.getProductById(productId))
        }
    }

    

    fun updateProduct(product: com.pilarkreasi.pillarpos.data.model.ProductEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onDone()
        }
    }

    

    fun deleteProduct(
        product: com.pilarkreasi.pillarpos.data.model.ProductEntity,
        onDone: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                onDone()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun addCategory(category: com.pilarkreasi.pillarpos.data.model.CategoryEntity) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun ajukanVoid(transaction: TransactionEntity, reason: String) {
        viewModelScope.launch {
            repository.ajukanVoid(transaction, reason)
        }
    }

    fun approveVoid(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.approveVoid(transaction)
        }
    }

    fun rejectVoid(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.rejectVoid(transaction)
        }
    }

    fun updateProductStock(productId: Int, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock)
        }
    }

    fun checkout(
        transaction: TransactionEntity,
        details: List<TransactionDetailEntity>,
        onSaved: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val newId = repository.checkout(transaction, details)
            onSaved(newId)
        }
    }

    

    fun observeTransaction(id: Int) = repository.observeTransactionById(id).asLiveData()

    suspend fun getDetailsWithProduct(transactionId: Int) =
        repository.getDetailsWithProduct(transactionId)

    

    fun addDiscount(discount: DiscountEntity) {
        viewModelScope.launch {
            repository.insertDiscount(discount)
        }
    }

    

    fun updateDiscount(discount: DiscountEntity) {
        viewModelScope.launch {
            repository.updateDiscount(discount)
        }
    }

    

    fun deleteDiscount(discount: DiscountEntity) {
        viewModelScope.launch {
            repository.deleteDiscount(discount)
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}