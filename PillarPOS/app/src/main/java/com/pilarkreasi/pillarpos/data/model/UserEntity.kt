package com.pilarkreasi.pillarpos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val idUser: Int = 0,
    val idOutlet: Int,
    val nama: String,
    val username: String,
    val password: String,
    val peran: Role,
    val status: Boolean = true 
)
