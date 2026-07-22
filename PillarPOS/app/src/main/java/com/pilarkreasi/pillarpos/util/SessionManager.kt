package com.pilarkreasi.pillarpos.util

import android.content.Context
import com.pilarkreasi.pillarpos.data.model.Role


class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(
        idUser: Int,
        nama: String,
        username: String,
        peran: Role,
        idOutlet: Int,
        authToken: String? = null
    ) {
        val editor = prefs.edit()
            .putInt(KEY_ID_USER, idUser)
            .putString(KEY_NAMA, nama)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PERAN, peran.name)
            .putInt(KEY_ID_OUTLET, idOutlet)
            .putBoolean(KEY_LOGGED_IN, true)
        if (authToken != null) {
            editor.putString(KEY_TOKEN, authToken)
        }
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getRole(): Role? = prefs.getString(KEY_PERAN, null)?.let { Role.valueOf(it) }

    fun getNama(): String? = prefs.getString(KEY_NAMA, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getIdUser(): Int = prefs.getInt(KEY_ID_USER, 0)

    fun getIdOutlet(): Int = prefs.getInt(KEY_ID_OUTLET, 1) 

    

    fun getAuthToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "pillar_pos_session"
        private const val KEY_ID_USER = "id_user"
        private const val KEY_NAMA = "nama"
        private const val KEY_USERNAME = "username"
        private const val KEY_PERAN = "peran"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_ID_OUTLET = "id_outlet"
        private const val KEY_TOKEN = "auth_token"
    }
}