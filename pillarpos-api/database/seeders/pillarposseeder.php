<?php

namespace Database\Seeders;

use App\Models\Kategori;
use App\Models\Outlet;
use App\Models\Produk;
use App\Models\Stok;
use App\Models\UserApi;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class PillarPosSeeder extends Seeder
{
    /**
     * Seed data awal untuk testing API PillarPOS:
     * - 1 outlet (Kedai Kopi Nusantara, sesuai narasumber SRS)
     * - 1 user admin & 1 user kasir
     * - 3 kategori & beberapa produk beserta stoknya
     */
    public function run(): void
    {
        $outlet = Outlet::firstOrCreate(
            ['nama_outlet' => 'Kedai Kopi Nusantara'],
            [
                'alamat'  => 'Jl. Diponegoro No. 52-60, Salatiga, Jawa Tengah',
                'telepon' => '081234567890',
            ]
        );

        UserApi::firstOrCreate(
            ['username' => 'admin'],
            [
                'id_outlet' => $outlet->id_outlet,
                'nama'      => 'Andika Pratama',
                'password'  => Hash::make('admin123'),
                'peran'     => 'admin',
                'status'    => true,
            ]
        );

        UserApi::firstOrCreate(
            ['username' => 'kasir1'],
            [
                'id_outlet' => $outlet->id_outlet,
                'nama'      => 'Sri Wulandari',
                'password'  => Hash::make('kasir123'),
                'peran'     => 'kasir',
                'status'    => true,
            ]
        );

        $kategoriMakanan  = Kategori::firstOrCreate(['nama_kategori' => 'Makanan']);
        $kategoriMinuman  = Kategori::firstOrCreate(['nama_kategori' => 'Minuman']);
        Kategori::firstOrCreate(['nama_kategori' => 'Snack']);

        $produkList = [
            ['nama_produk' => 'Espresso',      'harga' => 20000, 'id_kategori' => $kategoriMinuman->id_kategori, 'stok' => 10],
            ['nama_produk' => 'Latte',         'harga' => 25000, 'id_kategori' => $kategoriMinuman->id_kategori, 'stok' => 10],
            ['nama_produk' => 'Gedang Goreng', 'harga' => 15000, 'id_kategori' => $kategoriMakanan->id_kategori, 'stok' => 10],
        ];

        foreach ($produkList as $item) {
            $produk = Produk::firstOrCreate(
                ['nama_produk' => $item['nama_produk']],
                [
                    'id_kategori' => $item['id_kategori'],
                    'harga'       => $item['harga'],
                ]
            );

            Stok::firstOrCreate(
                ['id_produk' => $produk->id_produk, 'id_outlet' => $outlet->id_outlet],
                ['jumlah_stok' => $item['stok'], 'updated_at' => now()]
            );
        }
    }
}