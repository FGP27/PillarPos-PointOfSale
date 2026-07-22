package com.pilarkreasi.pillarpos.data.repository

import com.pilarkreasi.pillarpos.data.local.CategoryDao
import com.pilarkreasi.pillarpos.data.local.DiscountDao
import com.pilarkreasi.pillarpos.data.local.ProductDao
import com.pilarkreasi.pillarpos.data.local.TransactionDao
import com.pilarkreasi.pillarpos.data.model.CategoryEntity
import com.pilarkreasi.pillarpos.data.model.ProductEntity
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException


class ProductRepositoryTest {

    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var discountDao: DiscountDao
    private lateinit var apiService: ApiService
    private lateinit var repository: ProductRepository

    private var koneksiTersedia = true

    @Before
    fun setUp() {
        productDao = mock()
        categoryDao = mock()
        transactionDao = mock()
        discountDao = mock()
        apiService = mock()
        repository = ProductRepository(
            productDao = productDao,
            categoryDao = categoryDao,
            transactionDao = transactionDao,
            discountDao = discountDao,
            apiService = apiService,
            isInternetAvailable = { koneksiTersedia },
            getIdOutlet = { 1 }
        )
    }

    private fun produkBaru() = ProductEntity(idProduct = 0, idCategory = 1, name = "Kopi", price = 15_000.0, stock = 10)

    

    @Test
    fun `insertProduct saat online dan sukses harus pakai idProduct dari server`() = runTest {
        koneksiTersedia = true
        val produk = produkBaru()
        val responseDto = ApiEnvelope(
            success = true,
            data = ProdukDto(idProduk = 55, idKategori = 1, namaProduk = "Kopi", harga = "15000.00")
        )
        whenever(apiService.createProduk(any())).thenReturn(Response.success(responseDto))

        repository.insertProduct(produk)

        verify(productDao).insertProduct(produk.copy(idProduct = 55))
        verify(productDao, never()).insertProduct(produk) 
    }

    @Test
    fun `insertProduct saat online tapi server gagal harus tetap simpan lokal apa adanya`() = runTest {
        koneksiTersedia = true
        val produk = produkBaru()
        whenever(apiService.createProduk(any())).thenReturn(
            Response.error(500, "error".toResponseBody(null))
        )

        repository.insertProduct(produk)

        verify(productDao).insertProduct(produk) 
    }

    @Test
    fun `insertProduct saat offline harus langsung simpan lokal tanpa panggil API`() = runTest {
        koneksiTersedia = false
        val produk = produkBaru()

        repository.insertProduct(produk)

        verify(productDao).insertProduct(produk)
        verifyNoInteractions(apiService)
    }

    @Test
    fun `insertProduct saat online tapi server timeout (IOException) harus tetap simpan lokal`() = runTest {
        koneksiTersedia = true
        val produk = produkBaru()
        whenever(apiService.createProduk(any())).thenAnswer { throw IOException("timeout") }

        repository.insertProduct(produk)

        verify(productDao).insertProduct(produk)
    }

    

    @Test
    fun `insertCategory saat online dan sukses harus pakai idCategory dari server`() = runTest {
        koneksiTersedia = true
        val kategori = CategoryEntity(idCategory = 0, name = "Minuman")
        val responseDto = ApiEnvelope(success = true, data = KategoriDto(idKategori = 7, namaKategori = "Minuman"))
        whenever(apiService.createKategori(any())).thenReturn(Response.success(responseDto))

        repository.insertCategory(kategori)

        verify(categoryDao).insertCategory(kategori.copy(idCategory = 7))
    }

    @Test
    fun `insertCategory saat offline harus simpan lokal tanpa panggil API`() = runTest {
        koneksiTersedia = false
        val kategori = CategoryEntity(idCategory = 0, name = "Minuman")

        repository.insertCategory(kategori)

        verify(categoryDao).insertCategory(kategori)
        verifyNoInteractions(apiService)
    }

    

    private fun transaksiContoh() = TransactionEntity(idUser = 1, idOutlet = 1, totalAmount = 15_000.0, paymentType = "Tunai")
    private fun detailContoh(idTransaction: Int = 1) =
        listOf(TransactionDetailEntity(idTransaction = idTransaction, idProduct = 1, quantity = 2, priceAtTime = 15_000.0))

    @Test
    fun `checkout selalu simpan transaksi lokal dan kurangi stok, online atau offline`() = runTest {
        koneksiTersedia = false
        val transaksi = transaksiContoh()
        val details = detailContoh()
        whenever(transactionDao.insertFullTransaction(transaksi, details)).thenReturn(1)

        repository.checkout(transaksi, details)

        verify(transactionDao).insertFullTransaction(transaksi, details)
        verify(productDao).reduceStock(1, 2)
    }

    @Test
    fun `checkout online dan sukses harus tandai transaksi sebagai synced`() = runTest {
        koneksiTersedia = true
        val transaksi = transaksiContoh()
        val details = detailContoh()
        whenever(transactionDao.insertFullTransaction(transaksi, details)).thenReturn(1)
        whenever(apiService.createTransaksi(any())).thenReturn(
            Response.success(ApiEnvelope(success = true, data = TransaksiResponseDto()))
        )

        repository.checkout(transaksi, details)

        verify(transactionDao).markAsSynced(eq(1), any())
    }

    @Test
    fun `checkout offline tidak boleh menandai synced dan tidak boleh panggil API`() = runTest {
        koneksiTersedia = false
        val transaksi = transaksiContoh()
        val details = detailContoh()
        whenever(transactionDao.insertFullTransaction(transaksi, details)).thenReturn(1)

        repository.checkout(transaksi, details)

        verify(transactionDao, never()).markAsSynced(any(), any())
        verifyNoInteractions(apiService)
    }

    @Test
    fun `checkout online tapi gagal terkirim tidak boleh menandai synced`() = runTest {
        koneksiTersedia = true
        val transaksi = transaksiContoh()
        val details = detailContoh()
        whenever(transactionDao.insertFullTransaction(transaksi, details)).thenReturn(1)
        whenever(apiService.createTransaksi(any())).thenAnswer { throw IOException("timeout") }

        repository.checkout(transaksi, details)

        verify(transactionDao, never()).markAsSynced(any(), any())
    }

    

    @Test
    fun `ajukanVoid harus ubah status jadi PENDING_VOID dengan alasan, dan TIDAK kembalikan stok`() = runTest {
        val transaksi = transaksiContoh().copy(idTransaction = 1, paymentStatus = "COMPLETED")

        repository.ajukanVoid(transaksi, "Salah input jumlah")

        verify(transactionDao).updateTransaction(
            transaksi.copy(paymentStatus = "PENDING_VOID", voidReason = "Salah input jumlah")
        )
        
        
        
        
        verify(productDao, never()).addStock(any(), any())
    }

    @Test
    fun `approveVoid harus ubah status jadi VOID dan kembalikan stok tiap item`() = runTest {
        val transaksi = transaksiContoh().copy(idTransaction = 1, paymentStatus = "PENDING_VOID")
        val details = detailContoh(idTransaction = 1)
        whenever(transactionDao.getDetailsByTransactionId(1)).thenReturn(details)

        repository.approveVoid(transaksi)

        verify(transactionDao).updateTransaction(transaksi.copy(paymentStatus = "VOID"))
        verify(productDao).addStock(1, 2) 
    }

    @Test
    fun `rejectVoid harus kembalikan status ke COMPLETED tanpa ubah stok`() = runTest {
        val transaksi = transaksiContoh().copy(idTransaction = 1, paymentStatus = "PENDING_VOID")

        repository.rejectVoid(transaksi)

        verify(transactionDao).updateTransaction(transaksi.copy(paymentStatus = "COMPLETED"))
        
        
        verify(productDao, never()).addStock(any(), any()) 
    }
}
