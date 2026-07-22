<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class UserApi extends Model
{
    use HasFactory, Notifiable, HasApiTokens;

    protected $table = 'user_apis';
    protected $primaryKey = 'id_user';

    protected $fillable = [
        'id_outlet', 'nama', 'username', 'password', 'peran', 'status',
    ];

    protected $hidden = [
        'password',
    ];

    protected function casts(): array
    {
        return [
            'status' => 'boolean',
        ];
    }

    public function outlet()
    {
        return $this->belongsTo(Outlet::class, 'id_outlet', 'id_outlet');
    }
}