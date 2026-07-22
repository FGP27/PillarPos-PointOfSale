package com.pilarkreasi.pillarpos.data.repository

import android.content.Context
import android.util.Log
import com.pilarkreasi.pillarpos.data.local.CategoryDao
import com.pilarkreasi.pillarpos.data.local.DiscountDao
import com.pilarkreasi.pillarpos.data.local.ProductDao
import com.pilarkreasi.pillarpos.data.local.TransactionDao
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.data.model.TransactionDetailEntity
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.remote.ApiService
import com.pilarkreasi.pillarpos.data.remote.RetrofitClient
import com.pilarkreasi.pillarpos.data.remote.dto.KategoriCreateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukCreateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukUpdateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiItemDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiRequestDto
import com.pilarkreasi.pillarpos.util.NetworkUtil
import com.pilarkreasi.pillarpos.util.SessionManager
import kotlinx.coroutines.flow.Flow
import java.io.IOException


class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val discountDao: DiscountDao,
    private val apiService: ApiService,
    private val isInternetAvailable: () -> Boolean,
    private val getIdOutlet: () -> Int
) {

    companion object {
        private const val TAG = "ProductRepository"

        fun create(
            context: Context,
            productDao: ProductDao,
            categoryDao: CategoryDao,
            transactionDao: TransactionDao,
            discountDao: DiscountDao
        ): ProductRepository {
            val appContext = context.applicationContext
            return ProductRepository(
                productDao = productDao,
                categoryDao = categoryDao,
                transactionDao = transactionDao,
                discountDao = discountDao,
                apiService = RetrofitClient.api,
                isInternetAvailable = { NetworkUtil.isInternetAvailable(appContext) },
                getIdOutlet = { SessionManager(appContext).getIdOutlet() }
            )
        }
    }
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val pendingVoidTransactions: Flow<List<TransactionEntity>> = transactionDao.getPendingVoidTransactions()
    val todayTotalSales: Flow<Double?> = transactionDao.getTodayTotalSales()
    val todayTransactionCount: Flow<Int> = transactionDao.getTodayTransactionCount()
    val allDiscounts: Flow<List<DiscountEntity>> = discountDao.getAllDiscounts()
    val activeDiscounts: Flow<List<DiscountEntity>> = discountDao.getActiveDiscounts()

    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(categoryId)
    }

    suspend fun getProductById(productId: Int): ProductEntity? {
        return productDao.getProductById(productId)
    }

    suspend fun insertProduct(product: ProductEntity) {
        if (isInternetAvailable()) {
            val idDariServer = tryCreateProductOnServer(product)
            if (idDariServer != null) {
                productDao.insertProduct(product.copy(idProduct = idDariServer))
                return
            }
        }
        productDao.insertProduct(product)
    }

    private suspend fun tryCreateProductOnServer(product: ProductEntity): Int? {
        return try {
            val response = apiService.createProduk(
                ProdukCreateRequestDto(
                    namaProduk = product.name,
                    harga = product.price,
                    idKategori = product.idCategory,
                    foto = product.imageUrl,
                    stokAwal = product.stock,
                    idOutlet = getIdOutlet()
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.idProduk
            } else {
                Log.w(TAG, "Gagal membuat produk di server (HTTP ${response.code()}), disimpan lokal saja")
                null
            }
        } catch (e: IOException) {
            Log.w(TAG, "Gagal membuat produk di server (${e.message}), disimpan lokal saja")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error tak terduga saat membuat produk di server", e)
            null
        }
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)

        if (isInternetAvailable()) {
            try {
                val response = apiService.updateProduk(
                    product.idProduct,
                    ProdukUpdateRequestDto(
                        namaProduk = product.name,
                        harga = product.price,
                        idKategori = product.idCategory,
                        foto = product.imageUrl
                    )
                )
                if (!response.isSuccessful || response.body()?.success != true) {
                    Log.w(TAG, "Gagal update produk di server (HTTP ${response.code()}), perubahan tetap tersimpan lokal")
                }
            } catch (e: IOException) {
                Log.w(TAG, "Gagal update produk di server (${e.message}), perubahan tetap tersimpan lokal")
            } catch (e: Exception) {
                Log.e(TAG, "Error tak terduga saat update produk di server", e)
            }
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)

        if (isInternetAvailable()) {
            try {
                val response = apiService.deleteProduk(product.idProduct)
                if (!response.isSuccessful || response.body()?.success != true) {
                    Log.w(TAG, "Gagal hapus produk di server (HTTP ${response.code()}), data lokal tetap terhapus")
                }
            } catch (e: IOException) {
                Log.w(TAG, "Gagal hapus produk di server (${e.message}), data lokal tetap terhapus")
            } catch (e: Exception) {
                Log.e(TAG, "Error tak terduga saat hapus produk di server", e)
            }
        }
    }

    

    suspend fun insertCategory(category: CategoryEntity) {
        if (isInternetAvailable()) {
            val idDariServer = tryCreateCategoryOnServer(category)
            if (idDariServer != null) {
                categoryDao.insertCategory(category.copy(idCategory = idDariServer))
                return
            }
        }
        categoryDao.insertCategory(category)
    }

    

    private suspend fun tryCreateCategoryOnServer(category: CategoryEntity): Int? {
        return try {
            val response = apiService.createKategori(
                KategoriCreateRequestDto(namaKategori = category.name)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.idKategori
            } else {
                Log.w(TAG, "Gagal membuat kategori di server (HTTP ${response.code()}), disimpan lokal saja")
                null
            }
        } catch (e: IOException) {
            Log.w(TAG, "Gagal membuat kategori di server (${e.message}), disimpan lokal saja")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error tak terduga saat membuat kategori di server", e)
            null
        }
    }

    suspend fun updateProductStock(productId: Int, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }

    

    suspend fun checkout(transaction: TransactionEntity, details: List<TransactionDetailEntity>): Int {
        val newId = transactionDao.insertFullTransaction(transaction, details)
        
        details.forEach { detail ->
            productDao.reduceStock(detail.idProduct, detail.quantity)
        }

        if (isInternetAvailable()) {
            trySyncTransactionNow(newId, transaction, details)
        }

        return newId
    }

    private suspend fun trySyncTransactionNow(
        localId: Int,
        transaction: TransactionEntity,
        details: List<TransactionDetailEntity>
    ) {
        try {
            val response = apiService.createTransaksi(
                TransaksiRequestDto(
                    idOutlet = transaction.idOutlet,
                    clientUuid = transaction.clientUuid,
                    isOffline = false,
                    items = details.map { TransaksiItemDto(idProduk = it.idProduct, jumlah = it.quantity) },
                    metodePembayaran = mapMetodePembayaranKeApi(transaction.paymentType),
                    jumlahBayar = transaction.totalAmount,
                    catatan = transaction.notes
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                transactionDao.markAsSynced(localId, System.currentTimeMillis())
            } else {
                Log.w(TAG, "Checkout online gagal (HTTP ${response.code()}), transaksi tetap tersimpan offline untuk disinkronkan nanti")
            }
        } catch (e: IOException) {
            Log.w(TAG, "Checkout online gagal (${e.message}), transaksi tetap tersimpan offline untuk disinkronkan nanti")
        } catch (e: Exception) {
            Log.e(TAG, "Error tak terduga saat checkout online", e)
        }
    }

    private fun mapMetodePembayaranKeApi(paymentType: String): String = when (paymentType.lowercase()) {
        "tunai" -> "tunai"
        "qris" -> "qris"
        "debit", "kartu" -> "kartu"
        else -> "tunai"
    }

    

    suspend fun ajukanVoid(transaction: TransactionEntity, reason: String) {
        val diajukan = transaction.copy(paymentStatus = "PENDING_VOID", voidReason = reason)
        transactionDao.updateTransaction(diajukan)
    }

    

    suspend fun approveVoid(transaction: TransactionEntity) {
        val disetujui = transaction.copy(paymentStatus = "VOID")
        transactionDao.updateTransaction(disetujui)

        
        val details = transactionDao.getDetailsByTransactionId(transaction.idTransaction)
        details.forEach { detail ->
            productDao.addStock(detail.idProduct, detail.quantity)
        }
    }

    

    suspend fun rejectVoid(transaction: TransactionEntity) {
        val ditolak = transaction.copy(paymentStatus = "COMPLETED")
        transactionDao.updateTransaction(ditolak)
    }

    

    fun getSalesSummary(startMillis: Long, endMillis: Long) =
        transactionDao.getSalesSummary(startMillis, endMillis)

    fun getDailySalesTrend(startMillis: Long, endMillis: Long) =
        transactionDao.getDailySalesTrend(startMillis, endMillis)

    fun getTopProducts(startMillis: Long, endMillis: Long) =
        transactionDao.getTopProducts(startMillis, endMillis)

    

    suspend fun getDetailsWithProduct(transactionId: Int) =
        transactionDao.getDetailsWithProductByTransactionId(transactionId)

    fun observeTransactionById(id: Int) = transactionDao.observeTransactionById(id)

    

    suspend fun insertDiscount(discount: DiscountEntity) {
        discountDao.insertDiscount(discount)
    }

    suspend fun updateDiscount(discount: DiscountEntity) {
        discountDao.updateDiscount(discount)
    }

    suspend fun deleteDiscount(discount: DiscountEntity) {
        discountDao.deleteDiscount(discount)
    }
}