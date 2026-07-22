<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Produk;
use App\Models\Stok;
use App\Models\Transaksi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi use case "Melakukan Transaksi Penjualan" & "Memproses Pembayaran"
 * (SRS Tabel 3.2, Flowchart 3.6.1).
 *
 * Mendukung mode offline: aplikasi Android membuat `client_uuid` sendiri saat
 * transaksi dibuat (baik online maupun offline). Saat sinkronisasi ulang terjadi
 * (mis. transaksi offline yang baru terkirim setelah koneksi kembali), endpoint
 * ini bersifat idempoten terhadap `client_uuid` sehingga tidak ada transaksi ganda.
 */
class TransaksiController extends Controller
{
    /** Melihat daftar transaksi (untuk laporan/riwayat). */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = Transaksi::with(['detailTransaksi.produk', 'pembayaran', 'outlet', 'kasir']);

        // Kasir hanya boleh melihat transaksi di outlet-nya sendiri.
        if ($user->peran === 'kasir') {
            $query->where('id_outlet', $user->id_outlet);
        } elseif ($request->filled('id_outlet')) {
            $query->where('id_outlet', $request->id_outlet);
        }

        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        if ($request->filled('tanggal_mulai')) {
            $query->whereDate('tanggal', '>=', $request->tanggal_mulai);
        }

        if ($request->filled('tanggal_selesai')) {
            $query->whereDate('tanggal', '<=', $request->tanggal_selesai);
        }

        $transaksi = $query->orderByDesc('tanggal')->paginate(20);

        return response()->json(['success' => true, 'data' => $transaksi]);
    }

    /** Melihat detail satu transaksi beserta item & pembayarannya. */
    public function show(Request $request, Transaksi $transaksi)
    {
        $user = $request->user();

        if ($user->peran === 'kasir' && $transaksi->id_outlet !== $user->id_outlet) {
            return response()->json(['success' => false, 'message' => 'Transaksi bukan milik outlet Anda'], 403);
        }

        $transaksi->load(['detailTransaksi.produk', 'pembayaran', 'outlet', 'kasir']);

        return response()->json(['success' => true, 'data' => $transaksi]);
    }

    /**
     * Membuat transaksi baru (checkout) + mencatat pembayaran + mengurangi stok.
     *
     * Body:
     * {
     *   "id_outlet": 1,
     *   "client_uuid": "uuid-v4-dibuat-di-android",   // wajib, kunci idempotensi
     *   "is_offline": false,                           // opsional, default false
     *   "tanggal": "2026-07-19 10:15:00",               // opsional, waktu transaksi asli
     *   "items": [ { "id_produk": 1, "jumlah": 2 }, ... ],
     *   "metode_pembayaran": "tunai|qris|kartu",
     *   "jumlah_bayar": 65000,
     *   "status_pembayaran": "sukses",                 // opsional (khusus qris/kartu, hasil dari payment gateway)
     *   "catatan": "opsional"
     * }
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'id_outlet'             => 'required|exists:outlets,id_outlet',
            'client_uuid'           => 'required|string|max:64',
            'is_offline'            => 'boolean',
            'tanggal'               => 'nullable|date',
            'items'                 => 'required|array|min:1',
            'items.*.id_produk'     => 'required|exists:produks,id_produk',
            'items.*.jumlah'        => 'required|integer|min:1',
            'metode_pembayaran'     => 'required|in:tunai,qris,kartu',
            'jumlah_bayar'          => 'required|numeric|min:0',
            'status_pembayaran'     => 'nullable|string',
            'catatan'               => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $user = $request->user();

        // Kasir hanya boleh bertransaksi di outlet miliknya sendiri.
        if ($user->peran === 'kasir' && (int) $request->id_outlet !== (int) $user->id_outlet) {
            return response()->json(['success' => false, 'message' => 'Anda hanya dapat bertransaksi di outlet Anda sendiri'], 403);
        }

        // Idempotensi: kalau client_uuid ini sudah pernah masuk (mis. retry sync
        // transaksi offline), langsung kembalikan transaksi yang sudah ada,
        // jangan buat duplikat.
        $existing = Transaksi::where('client_uuid', $request->client_uuid)->first();
        if ($existing) {
            $existing->load(['detailTransaksi.produk', 'pembayaran']);

            return response()->json([
                'success' => true,
                'message' => 'Transaksi sudah tersinkron sebelumnya',
                'data'    => $existing,
            ]);
        }

        try {
            $transaksi = DB::transaction(function () use ($request, $user) {
                $totalBayar = 0;
                $detailRows = [];

                foreach ($request->items as $item) {
                    // Lock baris stok supaya aman dari race condition antar transaksi bersamaan.
                    $stok = Stok::where('id_produk', $item['id_produk'])
                        ->where('id_outlet', $request->id_outlet)
                        ->lockForUpdate()
                        ->first();

                    if (! $stok || $stok->jumlah_stok < $item['jumlah']) {
                        // Skenario Alternatif 7.b Tabel 3.2: "Stok produk tidak mencukupi"
                        abort(response()->json([
                            'success' => false,
                            'message' => 'Stok produk tidak mencukupi',
                        ], 422));
                    }

                    $produk = Produk::findOrFail($item['id_produk']);
                    $subtotal = $produk->harga * $item['jumlah'];
                    $totalBayar += $subtotal;

                    $detailRows[] = [
                        'id_produk'     => $produk->id_produk,
                        'jumlah'        => $item['jumlah'],
                        'harga_satuan'  => $produk->harga,
                        'subtotal'      => $subtotal,
                    ];

                    $stok->decrement('jumlah_stok', $item['jumlah']);
                    $stok->update(['updated_at' => now()]);
                }

                $transaksi = Transaksi::create([
                    'id_outlet'   => $request->id_outlet,
                    'id_kasir'    => $user->id_user,
                    'tanggal'     => $request->tanggal ?? now(),
                    'total_bayar' => $totalBayar,
                    'status'      => 'completed',
                    'catatan'     => $request->catatan,
                    'client_uuid' => $request->client_uuid,
                    'is_offline'  => $request->boolean('is_offline'),
                    'synced_at'   => now(),
                ]);

                foreach ($detailRows as $row) {
                    $transaksi->detailTransaksi()->create($row);
                }

                $transaksi->pembayaran()->create([
                    'metode'             => $request->metode_pembayaran,
                    'jumlah_bayar'       => $request->jumlah_bayar,
                    'status_pembayaran'  => $request->status_pembayaran ?? 'sukses',
                ]);

                return $transaksi;
            });
        } catch (\Illuminate\Http\Exceptions\HttpResponseException $e) {
            return $e->getResponse();
        }

        $transaksi->load(['detailTransaksi.produk', 'pembayaran']);

        return response()->json([
            'success' => true,
            'message' => 'Transaksi berhasil',
            'data'    => $transaksi,
        ], 201);
    }
}