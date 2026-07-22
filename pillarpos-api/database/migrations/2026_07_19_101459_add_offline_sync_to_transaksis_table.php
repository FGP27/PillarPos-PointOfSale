<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('transaksis', function (Blueprint $table) {
            $table->string('client_uuid')->nullable()->unique()->after('id_kasir');
            $table->boolean('is_offline')->default(false)->after('status');
            $table->timestamp('synced_at')->nullable()->after('is_offline');
        });
    }
    
    public function down(): void
    {
        Schema::table('transaksis', function (Blueprint $table) {
            $table->dropColumn(['client_uuid', 'is_offline', 'synced_at']);
        });
    }
};
