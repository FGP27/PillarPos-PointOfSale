package com.pilarkreasi.pillarpos.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KategoriDto(
    @SerializedName("id_kategori") val idKategori: Int,
    @SerializedName("nama_kategori") val namaKategori: String
)


data class KategoriCreateRequestDto(
    @SerializedName("nama_kategori") val namaKategori: String
)


data class StokNestedDto(
    @SerializedName("id_stok") val idStok: Int,
    @SerializedName("id_produk") val idProduk: Int,
    @SerializedName("id_outlet") val idOutlet: Int,
    @SerializedName("jumlah_stok") val jumlahStok: Int
)

data class ProdukDto(
    @SerializedName("id_produk") val idProduk: Int,
    @SerializedName("id_kategori") val idKategori: Int?,
    @SerializedName("nama_produk") val namaProduk: String,
    val harga: String, 
    val foto: String? = null,
    val kategori: KategoriDto? = null,
    val stok: StokNestedDto? = null
) {
    fun hargaAsDouble(): Double = harga.toDoubleOrNull() ?: 0.0
}


data class StokDto(
    @SerializedName("id_stok") val idStok: Int,
    @SerializedName("id_produk") val idProduk: Int,
    @SerializedName("id_outlet") val idOutlet: Int,
    @SerializedName("jumlah_stok") val jumlahStok: Int,
    val produk: ProdukRingkasDto? = null
)

data class ProdukRingkasDto(
    @SerializedName("id_produk") val idProduk: Int,
    @SerializedName("nama_produk") val namaProduk: String
)

data class OutletDto(
    @SerializedName("id_outlet") val idOutlet: Int,
    @SerializedName("nama_outlet") val namaOutlet: String,
    val alamat: String? = null,
    val telepon: String? = null
)


data class ProdukCreateRequestDto(
    @SerializedName("nama_produk") val namaProduk: String,
    val harga: Double,
    @SerializedName("id_kategori") val idKategori: Int?,
    val foto: String? = null,
    @SerializedName("stok_awal") val stokAwal: Int,
    @SerializedName("id_outlet") val idOutlet: Int
)


data class ProdukUpdateRequestDto(
    @SerializedName("nama_produk") val namaProduk: String,
    val harga: Double,
    @SerializedName("id_kategori") val idKategori: Int?,
    val foto: String? = null
)