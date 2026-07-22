<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('diskons', function (Blueprint $table) {
            $table->id('id_diskon');
            $table->string('nama_diskon');
            $table->enum('tipe', ['produk', 'kategori', 'total']);
            $table->foreignId('id_produk')->nullable()->constrained('produks', 'id_produk');
            $table->foreignId('id_kategori')->nullable()->constrained('kategoris', 'id_kategori');
            $table->enum('tipe_nilai', ['persen', 'nominal'])->default('persen');
            $table->decimal('nilai_diskon', 12, 2);
            $table->date('periode_mulai')->nullable();
            $table->date('periode_selesai')->nullable();
            $table->boolean('aktif')->default(true);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('diskons');
    }
};