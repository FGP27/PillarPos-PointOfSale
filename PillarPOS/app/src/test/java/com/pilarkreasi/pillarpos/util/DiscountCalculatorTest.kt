package com.pilarkreasi.pillarpos.util

import com.pilarkreasi.pillarpos.data.model.CartItem
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.DiscountType
import com.pilarkreasi.pillarpos.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date


class DiscountCalculatorTest {

    
    private fun buatProduk(id: Int, harga: Double, categoryId: Int = 1) =
        Product(id = id, name = "Produk $id", price = harga, categoryId = categoryId)

    private fun tanggal(tahun: Int, bulan: Int, tanggal: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(tahun, bulan - 1, tanggal, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    @Test
    fun `tanpa diskon aktif, final total harus sama dengan subtotal`() {
        val cart = listOf(CartItem(buatProduk(id = 1, harga = 10_000.0), quantity = 2))

        val result = DiscountCalculator.calculate(cart, discounts = emptyList())

        assertEquals(20_000.0, result.subtotal, 0.0)
        assertEquals(0.0, result.discountAmount, 0.0)
        assertEquals(20_000.0, result.finalTotal, 0.0)
        assertTrue(result.appliedDiscountNames.isEmpty())
    }

    @Test
    fun `diskon PRODUCT harus diterapkan hanya ke produk yang cocok`() {
        val produkA = buatProduk(id = 1, harga = 10_000.0)
        val produkB = buatProduk(id = 2, harga = 5_000.0)
        val cart = listOf(
            CartItem(produkA, quantity = 1), 
            CartItem(produkB, quantity = 1)  
        )
        val diskon = DiscountEntity(
            name = "Diskon Produk A 10%",
            type = DiscountType.PRODUCT,
            targetId = produkA.id,
            percentage = 10.0
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskon))

        
        assertEquals(15_000.0, result.subtotal, 0.0)
        assertEquals(1_000.0, result.discountAmount, 0.0)
        assertEquals(14_000.0, result.finalTotal, 0.0)
        assertEquals(listOf("Diskon Produk A 10%"), result.appliedDiscountNames)
    }

    @Test
    fun `jika produk kena diskon PRODUCT dan CATEGORY, pakai yang persentasenya lebih besar`() {
        val produk = buatProduk(id = 1, harga = 100_000.0, categoryId = 5)
        val cart = listOf(CartItem(produk, quantity = 1))

        val diskonProduk = DiscountEntity(
            name = "Diskon Produk 5%",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 5.0
        )
        val diskonKategori = DiscountEntity(
            name = "Diskon Kategori 20%",
            type = DiscountType.CATEGORY,
            targetId = produk.categoryId,
            percentage = 20.0
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskonProduk, diskonKategori))

        
        assertEquals(20_000.0, result.discountAmount, 0.0)
        assertEquals(80_000.0, result.finalTotal, 0.0)
        assertEquals(listOf("Diskon Kategori 20%"), result.appliedDiscountNames)
    }

    @Test
    fun `diskon TOTAL diterapkan ke subtotal SISA setelah diskon per-item`() {
        val produk = buatProduk(id = 1, harga = 100_000.0)
        val cart = listOf(CartItem(produk, quantity = 1))

        val diskonProduk = DiscountEntity(
            name = "Diskon Produk 10%",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 10.0
        )
        val diskonTotal = DiscountEntity(
            name = "Diskon Total 10%",
            type = DiscountType.TOTAL,
            percentage = 10.0
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskonProduk, diskonTotal))

        
        
        
        assertEquals(19_000.0, result.discountAmount, 0.0)
        assertEquals(81_000.0, result.finalTotal, 0.0)
    }

    @Test
    fun `diskon yang isActive false harus diabaikan`() {
        val produk = buatProduk(id = 1, harga = 10_000.0)
        val cart = listOf(CartItem(produk, quantity = 1))
        val diskonNonAktif = DiscountEntity(
            name = "Promo Lama",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 50.0,
            isActive = false
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskonNonAktif))

        assertEquals(0.0, result.discountAmount, 0.0)
        assertEquals(10_000.0, result.finalTotal, 0.0)
    }

    @Test
    fun `diskon di luar periode validFrom-validUntil harus diabaikan`() {
        val produk = buatProduk(id = 1, harga = 10_000.0)
        val cart = listOf(CartItem(produk, quantity = 1))
        val diskonKedaluwarsa = DiscountEntity(
            name = "Promo Ramadan",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 50.0,
            validFrom = tanggal(2024, 1, 1),
            validUntil = tanggal(2024, 1, 31)
        )
        val sekarang = tanggal(2025, 6, 1) 

        val result = DiscountCalculator.calculate(cart, listOf(diskonKedaluwarsa), now = sekarang)

        assertEquals(0.0, result.discountAmount, 0.0)
        assertEquals(10_000.0, result.finalTotal, 0.0)
    }

    @Test
    fun `diskon yang validFrom dan validUntil null dianggap berlaku terus`() {
        val produk = buatProduk(id = 1, harga = 10_000.0)
        val cart = listOf(CartItem(produk, quantity = 1))
        val diskonPermanen = DiscountEntity(
            name = "Diskon Member",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 15.0,
            validFrom = null,
            validUntil = null
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskonPermanen))

        assertEquals(1_500.0, result.discountAmount, 0.0)
    }

    @Test
    fun `final total tidak boleh negatif walau diskon lebih besar dari subtotal`() {
        val produk = buatProduk(id = 1, harga = 10_000.0)
        val cart = listOf(CartItem(produk, quantity = 1))
        val diskonBesar = DiscountEntity(
            name = "Diskon 90%",
            type = DiscountType.PRODUCT,
            targetId = produk.id,
            percentage = 90.0
        )
        val diskonTotalBesar = DiscountEntity(
            name = "Diskon Total 50%",
            type = DiscountType.TOTAL,
            percentage = 50.0
        )

        val result = DiscountCalculator.calculate(cart, listOf(diskonBesar, diskonTotalBesar))

        assertTrue(result.finalTotal >= 0.0)
    }

    @Test
    fun `keranjang kosong harus menghasilkan subtotal dan total nol`() {
        val result = DiscountCalculator.calculate(cartItems = emptyList(), discounts = emptyList())

        assertEquals(0.0, result.subtotal, 0.0)
        assertEquals(0.0, result.finalTotal, 0.0)
    }
}
