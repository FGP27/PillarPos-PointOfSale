<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\PengajuanVoid;
use App\Models\Stok;
use App\Models\Transaksi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi use case "Membatalkan (Void) Transaksi" (SRS Tabel 3.5,
 * Flowchart 3.6.2). Kasir mengajukan void -> menunggu persetujuan admin ->
 * admin approve (stok dikembalikan) atau reject (transaksi tetap berlaku).
 */
class VoidController extends Controller
{
    /** Melihat daftar pengajuan void. */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = PengajuanVoid::with(['transaksi.detailTransaksi.produk', 'kasir', 'admin']);

        if ($user->peran === 'kasir') {
            // Kasir hanya melihat pengajuan void yang dia buat sendiri.
            $query->where('id_kasir', $user->id_user);
        } elseif ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        $pengajuan = $query->orderByDesc('created_at')->paginate(20);

        return response()->json(['success' => true, 'data' => $pengajuan]);
    }

    /** Melihat detail satu pengajuan void. */
    public function show(Request $request, PengajuanVoid $void)
    {
        $user = $request->user();

        if ($user->peran === 'kasir' && $void->id_kasir !== $user->id_user) {
            return response()->json(['success' => false, 'message' => 'Pengajuan void ini bukan milik Anda'], 403);
        }

        $void->load(['transaksi.detailTransaksi.produk', 'kasir', 'admin']);

        return response()->json(['success' => true, 'data' => $void]);
    }

    /**
     * Kasir mengajukan void atas sebuah transaksi.
     * (SRS Tabel 3.5, langkah 1-5: kasir isi alasan -> status "menunggu persetujuan admin")
     */
    public function ajukan(Request $request, Transaksi $transaksi)
    {
        $validator = Validator::make($request->all(), [
            'alasan' => 'required|string|max:500',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $user = $request->user();

        if ($user->peran === 'kasir' && $transaksi->id_outlet !== $user->id_outlet) {
            return response()->json(['success' => false, 'message' => 'Transaksi bukan milik outlet Anda'], 403);
        }

        if ($transaksi->status !== 'completed') {
            return response()->json([
                'success' => false,
                'message' => 'Hanya transaksi berstatus completed yang dapat diajukan void',
            ], 422);
        }

        // Cegah pengajuan dobel untuk transaksi yang sama.
        if ($transaksi->pengajuanVoid()->where('status', 'menunggu')->exists()) {
            return response()->json([
                'success' => false,
                'message' => 'Transaksi ini sudah memiliki pengajuan void yang menunggu persetujuan',
            ], 422);
        }

        $void = DB::transaction(function () use ($transaksi, $request, $user) {
            $pengajuan = PengajuanVoid::create([
                'id_transaksi' => $transaksi->id_transaksi,
                'id_kasir'     => $user->id_user,
                'alasan'       => $request->alasan,
                'status'       => 'menunggu',
            ]);

            $transaksi->update(['status' => 'pending_void']);

            return $pengajuan;
        });

        return response()->json([
            'success' => true,
            'message' => 'Pengajuan void terkirim, menunggu persetujuan admin',
            'data'    => $void,
        ], 201);
    }

    /**
     * Admin menyetujui pengajuan void.
     * (SRS Tabel 3.5, langkah 6-8: transaksi dibatalkan, stok dikembalikan)
     */
    public function approve(Request $request, PengajuanVoid $void)
    {
        if ($void->status !== 'menunggu') {
            return response()->json(['success' => false, 'message' => 'Pengajuan void ini sudah diproses sebelumnya'], 422);
        }

        $admin = $request->user();

        DB::transaction(function () use ($void, $admin) {
            $transaksi = $void->transaksi()->with('detailTransaksi')->first();

            // Kembalikan stok untuk setiap item di transaksi.
            foreach ($transaksi->detailTransaksi as $detail) {
                $stok = Stok::where('id_produk', $detail->id_produk)
                    ->where('id_outlet', $transaksi->id_outlet)
                    ->lockForUpdate()
                    ->first();

                if ($stok) {
                    $stok->increment('jumlah_stok', $detail->jumlah);
                    $stok->update(['updated_at' => now()]);
                }
            }

            $transaksi->update([
                'status'      => 'void',
                'alasan_void' => $void->alasan,
            ]);

            $void->update([
                'status'   => 'approve',
                'id_admin' => $admin->id_user,
            ]);
        });

        $void->load(['transaksi.detailTransaksi.produk', 'kasir', 'admin']);

        return response()->json([
            'success' => true,
            'message' => 'Void disetujui, stok telah dikembalikan',
            'data'    => $void,
        ]);
    }

    /**
     * Admin menolak pengajuan void.
     * (SRS Tabel 3.5, skenario alternatif 6.a: transaksi tetap berlaku)
     */
    public function reject(Request $request, PengajuanVoid $void)
    {
        if ($void->status !== 'menunggu') {
            return response()->json(['success' => false, 'message' => 'Pengajuan void ini sudah diproses sebelumnya'], 422);
        }

        $admin = $request->user();

        DB::transaction(function () use ($void, $admin) {
            $void->transaksi()->update(['status' => 'completed']);

            $void->update([
                'status'   => 'reject',
                'id_admin' => $admin->id_user,
            ]);
        });

        $void->load(['transaksi', 'kasir', 'admin']);

        return response()->json([
            'success' => true,
            'message' => 'Pengajuan void ditolak, transaksi tetap berlaku',
            'data'    => $void,
        ]);
    }
}