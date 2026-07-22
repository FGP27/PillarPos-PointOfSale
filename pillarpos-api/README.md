# PillarPOS API

Backend RESTful API untuk aplikasi Point of Sale (POS) dibangun dengan Laravel 12 dan PHP 8.2+.

## Fitur

- Autentikasi menggunakan Laravel Sanctum
- Manajemen Kategori, Produk, Stok, dan Diskon
- Transaksi Penjualan & Pembayaran
- Void Transaksi (ajukan, approve, reject)
- Manajemen Pegawai (Admin only)
- Pengaturan Outlet
- Dashboard & Laporan Penjualan

## Persyaratan

- PHP 8.2 atau lebih tinggi
- Composer
- SQLite (default) atau MySQL/PostgreSQL

## Instalasi & Menjalankan

### 1. Install Dependencies

```bash
composer install
```

### 2. Setup Environment

```bash
copy .env.example .env
php artisan key:generate
```

### 3. Konfigurasi Database

Edit file `.env`, sesuaikan konfigurasi database. Default menggunakan SQLite:

```
DB_CONNECTION=sqlite
```

Untuk MySQL, ubah menjadi:

```
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=pillarpos
DB_USERNAME=root
DB_PASSWORD=
```

### 4. Jalankan Migrasi

```bash
php artisan migrate --force
```

### 5. Jalankan Server

```bash
php artisan serve
```

API akan tersedia di `http://localhost:8000`

### Menjalankan Semua Service Sekaligus

```bash
composer dev
```

Perintah ini menjalankan secara bersamaan:
- `php artisan serve` (server utama)
- `php artisan queue:listen` (queue worker)
- `php artisan pail` (log viewer)
- `npm run dev` (Vite dev server)

## Testing

### Jalankan Semua Test

```bash
composer test
```

### Jalankan Test Manual

```bash
php artisan test
```

### Jalankan Test Unit Saja

```bash
php artisan test --testsuite=Unit
```

### Jalankan Test Feature Saja

```bash
php artisan test --testsuite=Feature
```

## API Endpoints

| Method | Endpoint | Deskripsi | Akses |
|--------|----------|-----------|-------|
| POST | `/api/login` | Login | Public |
| POST | `/api/logout` | Logout | Auth |
| GET | `/api/me` | Data user login | Auth |
| GET | `/api/kategori` | List kategori | Auth |
| POST | `/api/kategori` | Tambah kategori | Admin |
| GET | `/api/produk` | List produk | Auth |
| POST | `/api/produk` | Tambah produk | Admin |
| PUT | `/api/produk/{id}` | Update produk | Admin |
| DELETE | `/api/produk/{id}` | Hapus produk | Admin |
| GET | `/api/stok` | List stok | Auth |
| PUT | `/api/stok/{id}` | Update stok | Admin |
| GET | `/api/diskon` | List diskon | Auth |
| POST | `/api/diskon` | Tambah diskon | Admin |
| PUT | `/api/diskon/{id}` | Update diskon | Admin |
| DELETE | `/api/diskon/{id}` | Hapus diskon | Admin |
| GET | `/api/transaksi` | List transaksi | Auth |
| GET | `/api/transaksi/{id}` | Detail transaksi | Auth |
| POST | `/api/transaksi` | Buat transaksi | Auth |
| GET | `/api/void` | List void | Auth |
| GET | `/api/void/{id}` | Detail void | Auth |
| POST | `/api/transaksi/{id}/void` | Ajukan void | Auth |
| PUT | `/api/void/{id}/approve` | Approve void | Admin |
| PUT | `/api/void/{id}/reject` | Reject void | Admin |
| GET | `/api/outlet` | Lihat outlet | Auth |
| PUT | `/api/outlet` | Update outlet | Admin |
| GET | `/api/pegawai` | List pegawai | Admin |
| POST | `/api/pegawai` | Tambah pegawai | Admin |
| PUT | `/api/pegawai/{id}` | Update pegawai | Admin |
| GET | `/api/dashboard` | Data dashboard | Admin |
| GET | `/api/laporan` | Laporan penjualan | Admin |
