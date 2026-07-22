package com.pilarkreasi.pillarpos.data.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val categoryId: Int,
    val imageUrl: String? = null,
    val stock: Int = 0
)
