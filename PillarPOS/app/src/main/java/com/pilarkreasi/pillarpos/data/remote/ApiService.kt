package com.pilarkreasi.pillarpos.data.remote

import com.pilarkreasi.pillarpos.data.remote.dto.ApiEnvelope
import com.pilarkreasi.pillarpos.data.remote.dto.KategoriCreateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.KategoriDto
import com.pilarkreasi.pillarpos.data.remote.dto.LoginRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.LoginResponseDto
import com.pilarkreasi.pillarpos.data.remote.dto.OutletDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukCreateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukDto
import com.pilarkreasi.pillarpos.data.remote.dto.ProdukUpdateRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.StokDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiRequestDto
import com.pilarkreasi.pillarpos.data.remote.dto.TransaksiResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<Unit>>

    @GET("kategori")
    suspend fun getKategori(): Response<ApiEnvelope<List<KategoriDto>>>

    @POST("kategori")
    suspend fun createKategori(@Body request: KategoriCreateRequestDto): Response<ApiEnvelope<KategoriDto>>

    @GET("produk")
    suspend fun getProduk(): Response<ApiEnvelope<List<ProdukDto>>>

    @POST("produk")
    suspend fun createProduk(@Body request: ProdukCreateRequestDto): Response<ApiEnvelope<ProdukDto>>

    @PUT("produk/{id}")
    suspend fun updateProduk(@Path("id") idProduk: Int, @Body request: ProdukUpdateRequestDto): Response<ApiEnvelope<ProdukDto>>

    @DELETE("produk/{id}")
    suspend fun deleteProduk(@Path("id") idProduk: Int): Response<ApiEnvelope<Unit>>

    @GET("stok")
    suspend fun getStok(): Response<ApiEnvelope<List<StokDto>>>

    @GET("outlet")
    suspend fun getOutlet(): Response<ApiEnvelope<OutletDto>>

    @POST("transaksi")
    suspend fun createTransaksi(@Body request: TransaksiRequestDto): Response<ApiEnvelope<TransaksiResponseDto>>
}