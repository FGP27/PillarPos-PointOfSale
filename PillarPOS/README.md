# PillarPOS - Point of Sale Android

Aplikasi Android Point of Sale (POS) yang terhubung dengan PillarPOS API (Laravel backend).

## Fitur

- Login & Autentikasi dengan API
- Manajemen Kategori & Produk
- Keranjang Belanja (Cart) dengan perhitungan diskon
- Transaksi Penjualan & Pembayaran
- Void Transaksi
- Sinkronisasi data dengan server (offline-first dengan Room DB)

## Persyaratan

- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 17
- Android SDK 34
- Device/Emulator dengan minimum Android 9 (API 28)

## Instalasi & Menjalankan

### 1. Buka Project di Android Studio

```
File > Open > Pilih folder PillarPOS
```

### 2. Sync Gradle

Tunggu Android Studio selesai sync Gradle secara otomatis. Jika tidak, klik:

```
File > Sync Project with Gradle Files
```

### 3. Konfigurasi API Base URL

Buka file konfigurasi API (biasanya di `data/remote/` atau `network/`) dan sesuaikan base URL API:

```kotlin
// Sesuaikan dengan IP address mesin kamu
const val BASE_URL = "http://10.0.2.2:8000/api/"  // Untuk emulator
// const val BASE_URL = "http://<IP_KOMPUTER>:8000/api/"  // Untuk device fisik
```

> **Catatan untuk emulator:** `10.0.2.2` adalah alias untuk `localhost` komputer host.

### 4. Jalankan Aplikasi

Klik tombol **Run**  di Android Studio atau gunakan shortcut:

```
Shift + F10
```

Pilih device/emulator yang tersedia.

## Struktur Project

```
PillarPOS/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pilarkreasi/pillarpos/
│   │   │   │   ├── data/          # Repository, local DB, remote API
│   │   │   │   ├── ui/            # Activities, ViewModels, Adapters
│   │   │   │   └── util/          # Helper classes
│   │   │   └── res/               # Layouts, drawables, strings
│   │   └── test/                  # Unit tests
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Kotlin |
| Local Database | Room |
| Networking | Retrofit 2 + Gson |
| Architecture | MVVM |
| Coroutines | Kotlinx Coroutines |
| UI | Android Views + ViewBinding |

## Testing

### Jalankan Unit Test dari Android Studio

Klik kanan pada folder `app/src/test` > **Run Tests**

### Jalankan Unit Test dari Terminal

```bash
# Windows
gradlew.bat test

# macOS/Linux
./gradlew test
```

### Jalankan Specific Test Class

```bash
gradlew.bat test --tests "com.pilarkreasi.pillarpos.ui.main.CartViewModelTest"
```

### Test yang Tersedia

| Test File | Deskripsi |
|-----------|-----------|
| `CartViewModelTest` | Test logika keranjang belanja |
| `LoginViewModelTest` | Test proses login |
| `AuthRepositoryTest` | Test repository autentikasi |
| `ProductRepositoryTest` | Test repository produk |
| `SyncRepositoryTest` | Test sinkronisasi data |
| `DiscountCalculatorTest` | Test kalkulasi diskon |

## Catatan Penting

- Pastikan **PillarPOS API** sudah berjalan sebelum menggunakan aplikasi Android
- Untuk device fisik, pastikan komputer dan device berada di jaringan yang sama
- Aplikasi mendukung mode offline menggunakan Room database sebagai local storage
