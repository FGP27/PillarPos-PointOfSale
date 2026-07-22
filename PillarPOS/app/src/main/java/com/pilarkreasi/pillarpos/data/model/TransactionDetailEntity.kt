package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_detail",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["idTransaction"],
            childColumns = ["idTransaction"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["idProduct"],
            childColumns = ["idProduct"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class TransactionDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val idDetail: Int = 0,
    val idTransaction: Int,
    val idProduct: Int,
    val quantity: Int,
    val priceAtTime: Double 
)
