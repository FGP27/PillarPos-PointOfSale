package com.pilarkreasi.pillarpos.data.remote.dto

import com.google.gson.annotations.SerializedName


data class LoginRequestDto(
    val username: String,
    val password: String,
    val peran: String 
)


data class LoginResponseDto(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    @SerializedName("id_user") val idUser: Int,
    val nama: String,
    val username: String,
    val peran: String,
    @SerializedName("id_outlet") val idOutlet: Int
)