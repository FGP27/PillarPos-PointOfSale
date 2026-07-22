package com.pilarkreasi.pillarpos.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pilarkreasi.pillarpos.data.model.CartItem
import com.pilarkreasi.pillarpos.data.model.Product

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    fun addToCart(product: Product) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val existingItem = currentItems.find { it.product.id == product.id }

        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            currentItems.add(CartItem(product, 1))
        }

        _cartItems.value = currentItems
        calculateTotal()
    }

    fun removeFromCart(product: Product) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val existingItem = currentItems.find { it.product.id == product.id }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity -= 1
            } else {
                currentItems.remove(existingItem)
            }
        }

        _cartItems.value = currentItems
        calculateTotal()
    }

    private fun calculateTotal() {
        _totalPrice.value = _cartItems.value?.sumOf { it.totalPrice } ?: 0.0
    }

    fun getCartCount(): Int {
        return _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun clearCart() {
        _cartItems.value = mutableListOf()
        _totalPrice.value = 0.0
    }
}
