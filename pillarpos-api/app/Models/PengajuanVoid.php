<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PengajuanVoid extends Model
{
    protected $table = 'pengajuan_voids';
    protected $primaryKey = 'id_void';

    protected $fillable = [
        'id_transaksi', 'id_kasir', 'id_admin', 'alasan', 'status',
    ];

    public function transaksi()
    {
        return $this->belongsTo(Transaksi::class, 'id_transaksi', 'id_transaksi');
    }

    public function kasir()
    {
        return $this->belongsTo(UserApi::class, 'id_kasir', 'id_user');
    }

    public function admin()
    {
        return $this->belongsTo(UserApi::class, 'id_admin', 'id_user');
    }
}