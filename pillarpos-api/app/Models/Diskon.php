<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Diskon extends Model
{
    use HasFactory;

    protected $table = 'diskons';
    protected $primaryKey = 'id_diskon';
    protected $fillable = [
        'nama_diskon', 'tipe', 'id_produk', 'id_kategori',
        'tipe_nilai', 'nilai_diskon', 'periode_mulai', 'periode_selesai', 'aktif',
    ];

    protected function casts(): array
    {
        return ['aktif' => 'boolean'];
    }
}