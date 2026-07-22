<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('pengajuan_voids', function (Blueprint $table) {
            $table->id('id_void');
            $table->foreignId('id_transaksi')->constrained('transaksis', 'id_transaksi')->onDelete('cascade');
            $table->foreignId('id_kasir')->constrained('user_apis', 'id_user');
            $table->foreignId('id_admin')->nullable()->constrained('user_apis', 'id_user');
            $table->text('alasan');
            $table->enum('status', ['menunggu', 'approve', 'reject'])->default('menunggu');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('pengajuan_voids');
    }
};