package com.pilarkreasi.pillarpos.data.repository

import android.content.Context
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.local.UserDao
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.UserEntity
import com.pilarkreasi.pillarpos.data.remote.ApiService
import com.pilarkreasi.pillarpos.data.remote.RetrofitClient
import com.pilarkreasi.pillarpos.data.remote.dto.LoginRequestDto
import com.pilarkreasi.pillarpos.util.NetworkUtil
import java.io.IOException


class AuthRepository(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val isInternetAvailable: () -> Boolean
) {

    companion object {
        fun create(context: Context): AuthRepository {
            val appContext = context.applicationContext
            return AuthRepository(
                userDao = PillarPosDatabase.getInstance(appContext).userDao(),
                apiService = RetrofitClient.api,
                isInternetAvailable = { NetworkUtil.isInternetAvailable(appContext) }
            )
        }
    }

    suspend fun login(username: String, password: String, peran: Role): LoginResult {
        if (username.isBlank() || password.isBlank()) {
            return LoginResult.FieldKosong
        }

        val adaKoneksi = isInternetAvailable()

        if (adaKoneksi) {
            val hasilOnline = loginOnline(username, password, peran)
            if (hasilOnline != null) {
                return hasilOnline
            }
        }

        val sudahAdaAkunTersimpan = userDao.countUsers() > 0
        if (!adaKoneksi && !sudahAdaAkunTersimpan) {
            return LoginResult.TidakAdaKoneksi
        }

        val user = userDao.login(username.trim(), password, peran)
        return if (user != null) LoginResult.Success(user = user, idUser = user.idUser) else LoginResult.KredensialSalah
    }

    private suspend fun loginOnline(username: String, password: String, peran: Role): LoginResult? {
        return try {
            val response = apiService.login(
                LoginRequestDto(username = username.trim(), password = password, peran = peran.name.lowercase())
            )

            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.user != null && body.token != null) {
                val dto = body.user
                val existingLocal = userDao.findByUsername(dto.username)
                val cachedUser = UserEntity(
                    idUser = existingLocal?.idUser ?: 0,
                    idOutlet = dto.idOutlet,
                    nama = dto.nama,
                    username = dto.username,
                    password = password, 
                    peran = Role.valueOf(dto.peran.uppercase()),
                    status = true
                )
                userDao.insertUser(cachedUser)

                LoginResult.Success(
                    user = cachedUser,
                    idUser = dto.idUser,
                    authToken = body.token,
                    idOutlet = dto.idOutlet
                )
            } else {
                when (response.code()) {
                    422 -> LoginResult.FieldKosong
                    else -> LoginResult.KredensialSalah
                }
            }
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}