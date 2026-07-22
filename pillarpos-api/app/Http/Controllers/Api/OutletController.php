<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Outlet;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi use case "Mengelola Pengaturan Outlet" (SRS Fitur No. 14):
 * "Admin dapat mengatur data dan konfigurasi outlet melalui menu Pengaturan
 * Outlet pada halaman Profile."
 *
 * Setiap pegawai (admin/kasir) sudah terikat ke satu outlet lewat kolom
 * id_outlet di user_apis, jadi endpoint ini bekerja atas outlet milik
 * pengguna yang sedang login -- bukan CRUD bebas semua outlet.
 */
class OutletController extends Controller
{
    /** Melihat data outlet milik pengguna yang sedang login (Admin & Kasir). */
    public function show(Request $request)
    {
        $outlet = Outlet::findOrFail($request->user()->id_outlet);

        return response()->json(['success' => true, 'data' => $outlet]);
    }

    /** Mengubah data & konfigurasi outlet milik sendiri (khusus Admin). */
    public function update(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_outlet' => 'sometimes|required|string|max:150',
            'alamat'      => 'nullable|string|max:255',
            'telepon'     => 'nullable|string|max:30',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $outlet = Outlet::findOrFail($request->user()->id_outlet);
        $outlet->update($request->only(['nama_outlet', 'alamat', 'telepon']));

        return response()->json([
            'success' => true,
            'message' => 'Pengaturan outlet berhasil diperbarui',
            'data'    => $outlet,
        ]);
    }
}