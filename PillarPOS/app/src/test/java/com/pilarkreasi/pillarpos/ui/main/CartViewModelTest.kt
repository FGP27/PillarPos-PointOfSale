package com.pilarkreasi.pillarpos.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pilarkreasi.pillarpos.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class CartViewModelTest {

    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CartViewModel
    private lateinit var produkA: Product
    private lateinit var produkB: Product

    @Before
    fun setUp() {
        viewModel = CartViewModel()
        produkA = Product(id = 1, name = "Kopi Susu", price = 15_000.0, categoryId = 1)
        produkB = Product(id = 2, name = "Roti Bakar", price = 10_000.0, categoryId = 2)
    }

    @Test
    fun `addToCart produk baru harus menambah item baru dengan quantity 1`() {
        viewModel.addToCart(produkA)

        val items = viewModel.cartItems.value
        assertEquals(1, items?.size)
        assertEquals(1, items?.first()?.quantity)
        assertEquals(15_000.0, viewModel.totalPrice.value)
    }

    @Test
    fun `addToCart produk yang sama harus menambah quantity, bukan bikin item baru`() {
        viewModel.addToCart(produkA)
        viewModel.addToCart(produkA)
        viewModel.addToCart(produkA)

        val items = viewModel.cartItems.value
        assertEquals(1, items?.size) 
        assertEquals(3, items?.first()?.quantity) 
        assertEquals(45_000.0, viewModel.totalPrice.value)
    }

    @Test
    fun `totalPrice harus terhitung benar untuk beberapa produk berbeda`() {
        viewModel.addToCart(produkA) 
        viewModel.addToCart(produkB) 
        viewModel.addToCart(produkB) 

        assertEquals(35_000.0, viewModel.totalPrice.value)
        assertEquals(3, viewModel.getCartCount()) 
    }

    @Test
    fun `removeFromCart mengurangi quantity jika lebih dari 1`() {
        viewModel.addToCart(produkA)
        viewModel.addToCart(produkA) 

        viewModel.removeFromCart(produkA)

        val items = viewModel.cartItems.value
        assertEquals(1, items?.size)
        assertEquals(1, items?.first()?.quantity)
    }

    @Test
    fun `removeFromCart menghapus item kalau quantity tinggal 1`() {
        viewModel.addToCart(produkA) 

        viewModel.removeFromCart(produkA)

        val items = viewModel.cartItems.value
        assertEquals(0, items?.size)
        assertEquals(0.0, viewModel.totalPrice.value)
    }

    @Test
    fun `removeFromCart produk yang tidak ada di cart tidak boleh error atau ubah apapun`() {
        viewModel.addToCart(produkA)

        viewModel.removeFromCart(produkB) 

        assertEquals(1, viewModel.cartItems.value?.size)
    }

    @Test
    fun `clearCart mengosongkan semua item dan reset total ke 0`() {
        viewModel.addToCart(produkA)
        viewModel.addToCart(produkB)

        viewModel.clearCart()

        assertEquals(0, viewModel.cartItems.value?.size)
        assertEquals(0.0, viewModel.totalPrice.value)
        assertEquals(0, viewModel.getCartCount())
    }
}
