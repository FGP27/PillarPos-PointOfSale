# PillarPOS - Point of Sale Android

Aplikasi Android Point of Sale (POS) yang terhubung dengan PillarPOS API (Laravel backend).

# Fitur

- Login & Autentikasi dengan API
- Manajemen Kategori & Produk
- Keranjang Belanja (Cart) dengan perhitungan diskon
- Transaksi Penjualan & Pembayaran
- Void Transaksi
- Sinkronisasi data dengan server (offline-first dengan Room DB)

# Instalasi & Menjalankan

# 1. Buka Project di Android Studio

File > Open > Pilih folder PillarPOS

# 2. Sync Gradle

Tunggu Android Studio selesai sync Gradle secara otomatis. Jika tidak, klik:
File > Sync Project with Gradle Files

# 3. Konfigurasi API Base URL

Buka file konfigurasi API (biasanya di `data/remote/` atau `network/`) dan sesuaikan base URL API:
const val BASE_URL = "http://10.0.2.2:8000/api/"  // Untuk emulator

# 4. Jalankan Aplikasi

Pilih device/emulator yang tersedia.

# Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Kotlin |
| Local Database | Room |
| Networking | Retrofit 2 + Gson |
| Architecture | MVVM |
| Coroutines | Kotlinx Coroutines |
| UI | Android Views + ViewBinding |
