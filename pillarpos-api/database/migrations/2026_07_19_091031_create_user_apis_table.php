<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('user_apis', function (Blueprint $table) {
            $table->id('id_user');
            $table->foreignId('id_outlet')->constrained('outlets', 'id_outlet');
            $table->string('nama');
            $table->string('username')->unique();
            $table->string('password'); // di-hash pakai Hash::make()
            $table->enum('peran', ['admin', 'kasir']);
            $table->boolean('status')->default(true); // aktif/nonaktif
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('user_apis');
    }
};