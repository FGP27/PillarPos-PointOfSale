<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Pembayaran extends Model
{
    protected $table = 'pembayarans';
    protected $primaryKey = 'id_pembayaran';

    protected $fillable = [
        'id_transaksi', 'metode', 'jumlah_bayar', 'status_pembayaran',
    ];

    protected function casts(): array
    {
        return [
            'jumlah_bayar' => 'decimal:2',
        ];
    }

    public function transaksi()
    {
        return $this->belongsTo(Transaksi::class, 'id_transaksi', 'id_transaksi');
    }
}