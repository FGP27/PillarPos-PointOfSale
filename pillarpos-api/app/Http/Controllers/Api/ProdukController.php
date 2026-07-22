<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Produk;
use App\Models\Stok;
use Illuminate\Database\QueryException;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi API "Mengelola Data Produk" (SRS Fitur No. 2, Tabel 3.3).
 * GET dibuka untuk Admin & Kasir (Kasir mode lihat saja - SRS 3.5.2 poin 8),
 * POST/PUT/DELETE khusus Admin (middleware 'admin' di routes/api.php).
 */
class ProdukController extends Controller
{
    public function index(Request $request)
    {
        $query = Produk::with(['kategori', 'stok']);

        // Mendukung fitur Search + filter kategori di halaman Product (SRS Gambar 3.3)
        if ($request->filled('search')) {
            $query->where('nama_produk', 'like', '%' . $request->search . '%');
        }
        if ($request->filled('id_kategori')) {
            $query->where('id_kategori', $request->id_kategori);
        }

        return response()->json(['success' => true, 'data' => $query->get()]);
    }

    public function store(Request $request)
    {
        // Skenario Alternatif 3.a: "Nama dan harga produk wajib diisi"
        $validator = Validator::make($request->all(), [
            'nama_produk' => 'required|string',
            'harga' => 'required|numeric|min:0',
            'id_kategori' => 'nullable|exists:kategoris,id_kategori',
            'foto' => 'nullable|string',
            'stok_awal' => 'nullable|integer|min:0',
            'id_outlet' => 'required|exists:outlets,id_outlet',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        // Skenario Alternatif 3.b: "Produk dengan nama ini sudah ada" (dalam kategori yang sama)
        $duplikat = Produk::where('nama_produk', $request->nama_produk)
            ->where('id_kategori', $request->id_kategori)
            ->exists();

        if ($duplikat) {
            return response()->json(['success' => false, 'message' => 'Produk dengan nama ini sudah ada'], 422);
        }

        $produk = Produk::create($request->only('id_kategori', 'nama_produk', 'harga', 'foto'));

        Stok::create([
            'id_produk' => $produk->id_produk,
            'id_outlet' => $request->id_outlet,
            'jumlah_stok' => $request->stok_awal ?? 0,
            'updated_at' => now(),
        ]);

        return response()->json(['success' => true, 'message' => 'Produk berhasil ditambahkan', 'data' => $produk->load('stok')], 201);
    }

    public function update(Request $request, Produk $produk)
    {
        $validator = Validator::make($request->all(), [
            'nama_produk' => 'required|string',
            'harga' => 'required|numeric|min:0',
            'id_kategori' => 'nullable|exists:kategoris,id_kategori',
            'foto' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $produk->update($request->only('id_kategori', 'nama_produk', 'harga', 'foto'));

        return response()->json(['success' => true, 'message' => 'Produk berhasil diubah', 'data' => $produk]);
    }

    public function destroy(Produk $produk)
    {
        try {
            $produk->delete();
            return response()->json(['success' => true, 'message' => 'Produk berhasil dihapus']);
        } catch (QueryException $e) {
            // Produk masih direferensikan detail_transaksis (foreign key restrict)
            return response()->json([
                'success' => false,
                'message' => 'Produk tidak dapat dihapus karena masih memiliki riwayat transaksi',
            ], 409);
        }
    }
}