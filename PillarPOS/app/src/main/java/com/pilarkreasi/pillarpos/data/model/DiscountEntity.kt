package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "discount")
data class DiscountEntity(
    @PrimaryKey(autoGenerate = true)
    val idDiscount: Int = 0,
    val name: String,
    val type: DiscountType,
    val targetId: Int? = null,
    val percentage: Double,
    val isActive: Boolean = true,
    val validFrom: java.util.Date? = null,
    val validUntil: java.util.Date? = null
)