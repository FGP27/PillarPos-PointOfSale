package com.pilarkreasi.pillarpos.data.repository

import android.content.Context
import android.util.Log
import com.pilarkreasi.pillarpos.data.local.CategoryDao
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.local.ProductDao
import com.pilarkreasi.pillarpos.data.local.TransactionDao
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.ProductEntity
import com.pilarkreasi.pillarpos.data.remote.ApiService
import com.pilarkreasi.pillarpos.data.remote.RetrofitClient
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiItemDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiRequestDto
import com.pilarkreasi.pillarpos.util.NetworkUtil
import java.io.IOException


class SyncRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
    private val apiService: ApiService,
    private val isInternetAvailable: () -> Boolean
) {

    companion object {
        private const val TAG = "SyncRepository"

        

        fun create(context: Context): SyncRepository {
            val appContext = context.applicationContext
            val db = PillarPosDatabase.getInstance(appContext)
            return SyncRepository(
                transactionDao = db.transactionDao(),
                categoryDao = db.categoryDao(),
                productDao = db.productDao(),
                apiService = RetrofitClient.api,
                isInternetAvailable = { NetworkUtil.isInternetAvailable(appContext) }
            )
        }
    }

    suspend fun syncPendingTransactions() {
        if (!isInternetAvailable()) return

        val pending = transactionDao.getUnsyncedTransactions()
        for (transaction in pending) {
            try {
                val details = transactionDao.getDetailsByTransactionId(transaction.idTransaction)
                if (details.isEmpty()) continue

                val response = apiService.createTransaksi(
                    TransaksiRequestDto(
                        idOutlet = transaction.idOutlet,
                        clientUuid = transaction.clientUuid,
                        isOffline = true,
                        items = details.map { TransaksiItemDto(idProduk = it.idProduct, jumlah = it.quantity) },
                        metodePembayaran = mapMetodePembayaran(transaction.paymentType),
                        jumlahBayar = transaction.totalAmount,
                        catatan = transaction.notes
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    transactionDao.markAsSynced(transaction.idTransaction, System.currentTimeMillis())
                } else {
                    Log.w(TAG, "Gagal sync transaksi ${transaction.clientUuid}: HTTP ${response.code()}")
                }
            } catch (e: IOException) {
                
                Log.w(TAG, "Sinkronisasi terhenti (tidak ada koneksi): ${e.message}")
                return
            } catch (e: Exception) {
                Log.e(TAG, "Error tak terduga saat sync transaksi ${transaction.clientUuid}", e)
            }
        }
    }

    suspend fun pullMasterData() {
        if (!isInternetAvailable()) return

        try {
            val kategoriResponse = apiService.getKategori()
            val kategoriList = kategoriResponse.body()?.data.orEmpty()
            if (kategoriResponse.isSuccessful && kategoriList.isNotEmpty()) {
                categoryDao.insertAll(
                    kategoriList.map { CategoryEntity(idCategory = it.idKategori, name = it.namaKategori) }
                )
            }

            val produkResponse = apiService.getProduk()
            val produkList = produkResponse.body()?.data.orEmpty()
            if (produkResponse.isSuccessful && produkList.isNotEmpty()) {
                produkList.forEach { dto ->
                    productDao.insertProduct(
                        ProductEntity(
                            idProduct = dto.idProduk,
                            idCategory = dto.idKategori,
                            name = dto.namaProduk,
                            price = dto.hargaAsDouble(),
                            stock = dto.stok?.jumlahStok ?: 0,
                            imageUrl = dto.foto
                        )
                    )
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Gagal menarik data master (tidak ada koneksi): ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error tak terduga saat menarik data master", e)
        }
    }

    private fun mapMetodePembayaran(paymentType: String): String = when (paymentType.lowercase()) {
        "tunai" -> "tunai"
        "qris" -> "qris"
        "debit", "kartu" -> "kartu"
        else -> "tunai"
    }
}