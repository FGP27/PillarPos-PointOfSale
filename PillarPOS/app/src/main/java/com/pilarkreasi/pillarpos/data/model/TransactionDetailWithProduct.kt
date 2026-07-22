package com.pilarkreasi.pillarpos.data.model


data class TransactionDetailWithProduct(
    val productName: String,
    val quantity: Int,
    val priceAtTime: Double
)
