<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Diskon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class DiskonController extends Controller
{
    public function index(Request $request)
    {
        $query = Diskon::query();

        // Dipakai DiscountCalculator di Android: ambil yang aktif saja
        if ($request->boolean('aktif_saja')) {
            $query->where('aktif', true);
        }

        return response()->json(['success' => true, 'data' => $query->get()]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_diskon' => 'required|string',
            'tipe' => 'required|in:produk,kategori,total',
            'id_produk' => 'nullable|exists:produks,id_produk',
            'id_kategori' => 'nullable|exists:kategoris,id_kategori',
            'tipe_nilai' => 'required|in:persen,nominal',
            'nilai_diskon' => 'required|numeric|min:0',
            'periode_mulai' => 'nullable|date',
            'periode_selesai' => 'nullable|date|after_or_equal:periode_mulai',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $diskon = Diskon::create($request->all());

        return response()->json(['success' => true, 'message' => 'Diskon berhasil ditambahkan', 'data' => $diskon], 201);
    }

    public function update(Request $request, Diskon $diskon)
    {
        $diskon->update($request->only(
            'nama_diskon', 'tipe', 'id_produk', 'id_kategori',
            'tipe_nilai', 'nilai_diskon', 'periode_mulai', 'periode_selesai', 'aktif'
        ));

        return response()->json(['success' => true, 'message' => 'Diskon berhasil diubah', 'data' => $diskon]);
    }

    public function destroy(Diskon $diskon)
    {
        $diskon->delete();
        return response()->json(['success' => true, 'message' => 'Diskon berhasil dihapus']);
    }
}