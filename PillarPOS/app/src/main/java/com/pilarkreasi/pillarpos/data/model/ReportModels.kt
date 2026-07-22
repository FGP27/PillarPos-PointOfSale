package com.pilarkreasi.pillarpos.data.model


data class SalesSummary(
    val totalSales: Double?,
    val totalTransactions: Int
)


data class DailySales(
    val day: String, 
    val total: Double
)


data class TopProduct(
    val name: String,
    val totalQty: Int
)
