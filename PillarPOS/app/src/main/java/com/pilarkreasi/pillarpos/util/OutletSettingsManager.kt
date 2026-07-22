package com.pilarkreasi.pillarpos.util

import android.content.Context


class OutletSettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getNamaOutlet(): String = prefs.getString(KEY_NAMA_OUTLET, DEFAULT_NAMA_OUTLET) ?: DEFAULT_NAMA_OUTLET

    fun getAlamat(): String = prefs.getString(KEY_ALAMAT, DEFAULT_ALAMAT) ?: DEFAULT_ALAMAT

    fun getTelepon(): String = prefs.getString(KEY_TELEPON, DEFAULT_TELEPON) ?: DEFAULT_TELEPON

    fun saveOutletSettings(namaOutlet: String, alamat: String, telepon: String) {
        prefs.edit()
            .putString(KEY_NAMA_OUTLET, namaOutlet)
            .putString(KEY_ALAMAT, alamat)
            .putString(KEY_TELEPON, telepon)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "pillar_pos_outlet_settings"
        private const val KEY_NAMA_OUTLET = "nama_outlet"
        private const val KEY_ALAMAT = "alamat"
        private const val KEY_TELEPON = "telepon"

        private const val DEFAULT_NAMA_OUTLET = "Kedai Kopi Nusantara"
        private const val DEFAULT_ALAMAT = "Jl. Diponegoro No. 52-60, Salatiga"
        private const val DEFAULT_TELEPON = ""
    }
}