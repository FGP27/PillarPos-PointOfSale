package com.pilarkreasi.pillarpos.data.remote.dto

import com.google.gson.annotations.SerializedName


data class TransaksiRequestDto(
    @SerializedName("id_outlet") val idOutlet: Int,
    @SerializedName("client_uuid") val clientUuid: String,
    @SerializedName("is_offline") val isOffline: Boolean,
    val items: List<TransaksiItemDto>,
    @SerializedName("metode_pembayaran") val metodePembayaran: String,
    @SerializedName("jumlah_bayar") val jumlahBayar: Double,
    val catatan: String? = null
)

data class TransaksiItemDto(
    @SerializedName("id_produk") val idProduk: Int,
    val jumlah: Int
)

data class TransaksiResponseDto(
    @SerializedName("id_transaksi") val idTransaksi: Int? = null,
    @SerializedName("client_uuid") val clientUuid: String? = null,
    val status: String? = null
)