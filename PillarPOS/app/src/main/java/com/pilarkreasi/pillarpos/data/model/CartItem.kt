package com.pilarkreasi.pillarpos.data.model

data class CartItem(
    val product: Product,
    var quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity
}
