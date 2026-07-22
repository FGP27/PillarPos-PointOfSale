package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val idTransaction: Int = 0,
    val idUser: Int, 
    val idOutlet: Int = 1,
    val transactionDate: Date = Date(),
    val totalAmount: Double,
    val paymentType: String, 
    val paymentStatus: String = "COMPLETED", 
    val voidReason: String? = null,
    val notes: String? = null,
    val clientUuid: String = UUID.randomUUID().toString(),
    val isSynced: Boolean = false,
    val syncedAt: Date? = null
)