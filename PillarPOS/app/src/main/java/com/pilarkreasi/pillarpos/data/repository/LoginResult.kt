package com.pilarkreasi.pillarpos.data.repository

import com.pilarkreasi.pillarpos.data.model.UserEntity


sealed class LoginResult {
    data class Success(
        val user: UserEntity,
        val idUser: Int,
        val authToken: String? = null,
        val idOutlet: Int = user.idOutlet
    ) : LoginResult()
    object FieldKosong : LoginResult()
    object KredensialSalah : LoginResult()
    object TidakAdaKoneksi : LoginResult()
}