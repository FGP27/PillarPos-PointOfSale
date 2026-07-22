<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Produk extends Model
{
    use HasFactory;

    protected $table = 'produks';
    protected $primaryKey = 'id_produk';
    protected $fillable = ['id_kategori', 'nama_produk', 'harga', 'foto'];

    public function kategori()
    {
        return $this->belongsTo(Kategori::class, 'id_kategori', 'id_kategori');
    }

    public function stok()
    {
        return $this->hasOne(Stok::class, 'id_produk', 'id_produk');
    }
}