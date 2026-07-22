package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "product",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["idCategory"],
            childColumns = ["idCategory"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val idProduct: Int = 0,
    val idCategory: Int?,
    val name: String,
    val price: Double,
    val stock: Int = 0,
    val imageUrl: String? = null,
    val description: String? = null
)
