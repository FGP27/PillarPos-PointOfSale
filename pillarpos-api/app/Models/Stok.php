<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Stok extends Model
{
    use HasFactory;

    protected $table = 'stoks';
    protected $primaryKey = 'id_stok';
    public $timestamps = false;
    protected $fillable = ['id_produk', 'id_outlet', 'jumlah_stok', 'updated_at'];

    public function produk()
    {
        return $this->belongsTo(Produk::class, 'id_produk', 'id_produk');
    }
}