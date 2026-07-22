<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\DiskonController;
use App\Http\Controllers\Api\KategoriController;
use App\Http\Controllers\Api\OutletController;
use App\Http\Controllers\Api\PegawaiController;
use App\Http\Controllers\Api\ProdukController;
use App\Http\Controllers\Api\StokController;
use App\Http\Controllers\Api\TransaksiController;
use App\Http\Controllers\Api\VoidController;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\LaporanController;

Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', [AuthController::class, 'me']);

    // Bisa diakses Admin & Kasir (Kasir mode lihat saja)
    Route::get('/kategori', [KategoriController::class, 'index']);
    Route::get('/produk', [ProdukController::class, 'index']);
    Route::get('/stok', [StokController::class, 'index']);
    Route::get('/diskon', [DiskonController::class, 'index']);

    // Transaksi Penjualan & Pembayaran (SRS Tabel 3.2)
    Route::get('/transaksi', [TransaksiController::class, 'index']);
    Route::get('/transaksi/{transaksi}', [TransaksiController::class, 'show']);
    Route::post('/transaksi', [TransaksiController::class, 'store']);

    // Void Transaksi - ajukan bisa diakses Kasir & Admin (SRS Tabel 3.5)
    Route::get('/void', [VoidController::class, 'index']);
    Route::get('/void/{void}', [VoidController::class, 'show']);
    Route::post('/transaksi/{transaksi}/void', [VoidController::class, 'ajukan']);

    // Pengaturan Outlet (SRS Fitur No. 14) - lihat boleh Kasir & Admin
    Route::get('/outlet', [OutletController::class, 'show']);

    // Khusus Admin
    Route::middleware('admin')->group(function () {
        Route::post('/kategori', [KategoriController::class, 'store']);

        Route::post('/produk', [ProdukController::class, 'store']);
        Route::put('/produk/{produk}', [ProdukController::class, 'update']);
        Route::delete('/produk/{produk}', [ProdukController::class, 'destroy']);

        Route::put('/stok/{stok}', [StokController::class, 'update']);

        Route::post('/diskon', [DiskonController::class, 'store']);
        Route::put('/diskon/{diskon}', [DiskonController::class, 'update']);
        Route::delete('/diskon/{diskon}', [DiskonController::class, 'destroy']);

        // Approve/reject void khusus Admin
        Route::put('/void/{void}/approve', [VoidController::class, 'approve']);
        Route::put('/void/{void}/reject', [VoidController::class, 'reject']);

        // Mengelola Data Pegawai (SRS Fitur No. 10)
        Route::get('/pegawai', [PegawaiController::class, 'index']);
        Route::get('/pegawai/{pegawai}', [PegawaiController::class, 'show']);
        Route::post('/pegawai', [PegawaiController::class, 'store']);
        Route::put('/pegawai/{pegawai}', [PegawaiController::class, 'update']);
        Route::put('/pegawai/{pegawai}/status', [PegawaiController::class, 'updateStatus']);

        // Ubah pengaturan outlet khusus Admin
        Route::put('/outlet', [OutletController::class, 'update']);
        
        // Dashboard & Laporan Penjualan (SRS Fitur No. 11 & 13)
        Route::get('/dashboard', [LaporanController::class, 'dashboard']);
        Route::get('/laporan', [LaporanController::class, 'index']);
    });
});