package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val idCategory: Int = 0,
    val name: String
)
