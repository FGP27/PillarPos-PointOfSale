<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Stok;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class StokController extends Controller
{
    /** Melihat Rekap Stok (use case di SRS 3.2). */
    public function index()
    {
        return response()->json(['success' => true, 'data' => Stok::with('produk')->get()]);
    }

    /** Menyesuaikan stok manual, khusus Admin (SRS Fitur No. 4). */
    public function update(Request $request, Stok $stok)
    {
        $validator = Validator::make($request->all(), [
            'jumlah_stok' => 'required|integer|min:0',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $stok->update(['jumlah_stok' => $request->jumlah_stok, 'updated_at' => now()]);

        return response()->json(['success' => true, 'message' => 'Stok berhasil diperbarui', 'data' => $stok]);
    }
}