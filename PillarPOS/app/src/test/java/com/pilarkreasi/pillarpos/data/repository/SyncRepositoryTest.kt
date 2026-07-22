package com.pilarkreasi.pillarpos.data.repository

import com.pilarkreasi.pillarpos.data.local.CategoryDao
import com.pilarkreasi.pillarpos.data.local.ProductDao
import com.pilarkreasi.pillarpos.data.local.TransactionDao
import com.pilarkreasi.pillarpos.data.model.TransactionDetailEntity
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.remote.ApiService
import com.pilarkreasi.pillarpos.data.remote.dto.ApiEnvelope
import com.pilarkreasi.pillarpos.data.remote.dto.KategoriDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiResponseDto
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException


class SyncRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var productDao: ProductDao
    private lateinit var apiService: ApiService
    private lateinit var repository: SyncRepository

    private var koneksiTersedia = true

    @Before
    fun setUp() {
        transactionDao = mock()
        categoryDao = mock()
        productDao = mock()
        apiService = mock()
        repository = SyncRepository(
            transactionDao = transactionDao,
            categoryDao = categoryDao,
            productDao = productDao,
            apiService = apiService,
            isInternetAvailable = { koneksiTersedia }
        )
    }

    private fun transaksiPending(id: Int) =
        TransactionEntity(idTransaction = id, idUser = 1, idOutlet = 1, totalAmount = 10_000.0, paymentType = "Tunai")

    

    @Test
    fun `syncPendingTransactions saat offline harus langsung berhenti tanpa panggil dao atau api`() = runTest {
        koneksiTersedia = false

        repository.syncPendingTransactions()

        verifyNoInteractions(transactionDao)
        verifyNoInteractions(apiService)
    }

    @Test
    fun `syncPendingTransactions dengan detail kosong harus dilewati (skip), tidak dikirim ke API`() = runTest {
        koneksiTersedia = true
        val transaksi = transaksiPending(1)
        whenever(transactionDao.getUnsyncedTransactions()).thenReturn(listOf(transaksi))
        whenever(transactionDao.getDetailsByTransactionId(1)).thenReturn(emptyList())

        repository.syncPendingTransactions()

        verifyNoInteractions(apiService)
        verify(transactionDao, never()).markAsSynced(any(), any())
    }

    @Test
    fun `syncPendingTransactions yang berhasil terkirim harus ditandai synced`() = runTest {
        koneksiTersedia = true
        val transaksi = transaksiPending(1)
        val details = listOf(TransactionDetailEntity(idTransaction = 1, idProduct = 1, quantity = 1, priceAtTime = 10_000.0))
        whenever(transactionDao.getUnsyncedTransactions()).thenReturn(listOf(transaksi))
        whenever(transactionDao.getDetailsByTransactionId(1)).thenReturn(details)
        whenever(apiService.createTransaksi(any())).thenReturn(
            Response.success(ApiEnvelope(success = true, data = TransaksiResponseDto()))
        )

        repository.syncPendingTransactions()

        verify(transactionDao).markAsSynced(any(), any())
    }

    @Test
    fun `syncPendingTransactions harus lanjut ke transaksi berikutnya kalau satu gagal (bukan IOException)`() = runTest {
        koneksiTersedia = true
        val transaksi1 = transaksiPending(1)
        val transaksi2 = transaksiPending(2)
        val details = listOf(TransactionDetailEntity(idTransaction = 1, idProduct = 1, quantity = 1, priceAtTime = 10_000.0))
        whenever(transactionDao.getUnsyncedTransactions()).thenReturn(listOf(transaksi1, transaksi2))
        whenever(transactionDao.getDetailsByTransactionId(any())).thenReturn(details)
        
        whenever(apiService.createTransaksi(any()))
            .thenReturn(Response.error(422, "invalid".toResponseBody(null)))
            .thenReturn(Response.success(ApiEnvelope(success = true, data = TransaksiResponseDto())))

        repository.syncPendingTransactions()

        
        verify(apiService, times(2)).createTransaksi(any())
        verify(transactionDao, times(1)).markAsSynced(any(), any()) 
    }

    @Test
    fun `syncPendingTransactions harus BERHENTI total kalau koneksi putus di tengah (IOException)`() = runTest {
        koneksiTersedia = true
        val transaksi1 = transaksiPending(1)
        val transaksi2 = transaksiPending(2)
        val details = listOf(TransactionDetailEntity(idTransaction = 1, idProduct = 1, quantity = 1, priceAtTime = 10_000.0))
        whenever(transactionDao.getUnsyncedTransactions()).thenReturn(listOf(transaksi1, transaksi2))
        whenever(transactionDao.getDetailsByTransactionId(any())).thenReturn(details)
        whenever(apiService.createTransaksi(any())).thenAnswer { throw IOException("koneksi putus") }

        repository.syncPendingTransactions()

        
        verify(apiService, times(1)).createTransaksi(any())
    }

    

    @Test
    fun `pullMasterData saat offline harus langsung berhenti tanpa panggil apapun`() = runTest {
        koneksiTersedia = false

        repository.pullMasterData()

        verifyNoInteractions(apiService)
        verifyNoInteractions(categoryDao)
        verifyNoInteractions(productDao)
    }

    @Test
    fun `pullMasterData sukses harus simpan kategori dan produk terbaru ke Room`() = runTest {
        koneksiTersedia = true
        whenever(apiService.getKategori()).thenReturn(
            Response.success(ApiEnvelope(success = true, data = listOf(KategoriDto(idKategori = 1, namaKategori = "Minuman"))))
        )
        whenever(apiService.getProduk()).thenReturn(
            Response.success(
                ApiEnvelope(
                    success = true,
                    data = listOf(ProdukDto(idProduk = 1, idKategori = 1, namaProduk = "Kopi", harga = "15000.00"))
                )
            )
        )

        repository.pullMasterData()

        verify(categoryDao).insertAll(any())
        verify(productDao).insertProduct(any())
    }

    @Test
    fun `pullMasterData dengan data kosong dari server tidak boleh menimpa Room dengan list kosong`() = runTest {
        koneksiTersedia = true
        whenever(apiService.getKategori()).thenReturn(Response.success(ApiEnvelope(success = true, data = emptyList())))
        whenever(apiService.getProduk()).thenReturn(Response.success(ApiEnvelope(success = true, data = emptyList())))

        repository.pullMasterData()

        verifyNoInteractions(categoryDao)
        verifyNoInteractions(productDao)
    }
}
