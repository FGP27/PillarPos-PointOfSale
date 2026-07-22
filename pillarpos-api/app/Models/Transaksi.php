<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Transaksi extends Model
{
    use HasFactory;

    protected $table = 'transaksis';
    protected $primaryKey = 'id_transaksi';

    protected $fillable = [
        'id_outlet', 'id_kasir', 'tanggal', 'total_bayar', 'status',
        'alasan_void', 'catatan', 'client_uuid', 'is_offline', 'synced_at',
    ];

    protected function casts(): array
    {
        return [
            'tanggal'    => 'datetime',
            'synced_at'  => 'datetime',
            'is_offline' => 'boolean',
            'total_bayar' => 'decimal:2',
        ];
    }

    public function outlet()
    {
        return $this->belongsTo(Outlet::class, 'id_outlet', 'id_outlet');
    }

    public function kasir()
    {
        return $this->belongsTo(UserApi::class, 'id_kasir', 'id_user');
    }

    public function detailTransaksi()
    {
        return $this->hasMany(DetailTransaksi::class, 'id_transaksi', 'id_transaksi');
    }

    public function pembayaran()
    {
        return $this->hasOne(Pembayaran::class, 'id_transaksi', 'id_transaksi');
    }

    public function pengajuanVoid()
    {
        return $this->hasOne(PengajuanVoid::class, 'id_transaksi', 'id_transaksi');
    }
}