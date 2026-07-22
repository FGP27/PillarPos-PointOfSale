<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\DetailTransaksi;
use App\Models\Transaksi;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi use case "Melihat Dashboard" (SRS Fitur No. 13) dan
 * "Melihat Laporan Penjualan" (SRS Fitur No. 11, Tabel 3.4). Khusus Admin
 * sesuai wireframe (Gambar 3.2) - menu ini tidak muncul di sisi Kasir.
 *
 * Hanya transaksi berstatus 'completed' yang dihitung (transaksi yang
 * di-void tidak masuk laporan penjualan).
 */
class LaporanController extends Controller
{
    /**
     * Dashboard: ringkasan penjualan hari ini + grafik 7 hari terakhir +
     * produk terlaris hari ini (Gambar 3.2: Dashboard).
     */
    public function dashboard(Request $request)
    {
        $user = $request->user();
        $idOutlet = $request->filled('id_outlet') ? $request->id_outlet : $user->id_outlet;

        $hariIni = now()->toDateString();

        $transaksiHariIni = Transaksi::where('id_outlet', $idOutlet)
            ->where('status', 'completed')
            ->whereDate('tanggal', $hariIni);

        $totalPenjualan = (clone $transaksiHariIni)->sum('total_bayar');
        $jumlahTransaksi = (clone $transaksiHariIni)->count();
        $rataRataTransaksi = $jumlahTransaksi > 0 ? round($totalPenjualan / $jumlahTransaksi, 2) : 0;

        // Grafik penjualan 7 hari terakhir.
        $grafikPenjualan = Transaksi::where('id_outlet', $idOutlet)
            ->where('status', 'completed')
            ->whereDate('tanggal', '>=', now()->subDays(6)->toDateString())
            ->selectRaw('DATE(tanggal) as tanggal, SUM(total_bayar) as total')
            ->groupBy('tanggal')
            ->orderBy('tanggal')
            ->get();

        // Produk terlaris hari ini.
        $produkTerlaris = DetailTransaksi::join('transaksis', 'transaksis.id_transaksi', '=', 'detail_transaksis.id_transaksi')
            ->where('transaksis.id_outlet', $idOutlet)
            ->where('transaksis.status', 'completed')
            ->whereDate('transaksis.tanggal', $hariIni)
            ->selectRaw('detail_transaksis.id_produk, SUM(detail_transaksis.jumlah) as total_terjual')
            ->groupBy('detail_transaksis.id_produk')
            ->orderByDesc('total_terjual')
            ->limit(5)
            ->with('produk:id_produk,nama_produk')
            ->get();

        return response()->json([
            'success' => true,
            'data' => [
                'total_penjualan_hari_ini'  => $totalPenjualan,
                'jumlah_transaksi_hari_ini' => $jumlahTransaksi,
                'rata_rata_transaksi'       => $rataRataTransaksi,
                'grafik_penjualan'          => $grafikPenjualan,
                'produk_terlaris'           => $produkTerlaris,
            ],
        ]);
    }

    /**
     * Laporan Penjualan per periode: harian / mingguan / bulanan
     * (SRS Tabel 3.4). Menampilkan total penjualan, grafik, produk terlaris,
     * dan performa kasir pada periode tersebut.
     *
     * Query params:
     * - periode: harian|mingguan|bulanan (default: harian)
     * - tanggal: tanggal acuan periode, format Y-m-d (default: hari ini)
     * - id_outlet: opsional, khusus kalau Admin mengelola lebih dari satu outlet
     */
    public function index(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'periode' => 'nullable|in:harian,mingguan,bulanan',
            'tanggal' => 'nullable|date',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $user = $request->user();
        $idOutlet = $request->filled('id_outlet') ? $request->id_outlet : $user->id_outlet;

        $periode = $request->periode ?? 'harian';
        $tanggalAcuan = $request->filled('tanggal') ? Carbon::parse($request->tanggal) : now();

        [$mulai, $selesai] = match ($periode) {
            'mingguan' => [$tanggalAcuan->copy()->startOfWeek(), $tanggalAcuan->copy()->endOfWeek()],
            'bulanan'  => [$tanggalAcuan->copy()->startOfMonth(), $tanggalAcuan->copy()->endOfMonth()],
            default    => [$tanggalAcuan->copy()->startOfDay(), $tanggalAcuan->copy()->endOfDay()],
        };

        $baseQuery = Transaksi::where('id_outlet', $idOutlet)
            ->where('status', 'completed')
            ->whereBetween('tanggal', [$mulai, $selesai]);

        $totalPenjualan = (clone $baseQuery)->sum('total_bayar');
        $jumlahTransaksi = (clone $baseQuery)->count();

        $ringkasanPeriode = [
            'periode'          => $periode,
            'tanggal_mulai'    => $mulai->toDateString(),
            'tanggal_selesai'  => $selesai->toDateString(),
            'total_penjualan'  => $totalPenjualan,
            'jumlah_transaksi' => $jumlahTransaksi,
        ];

        // Skenario Alternatif 4.a Tabel 3.4: "Belum ada data transaksi pada periode ini"
        if ($jumlahTransaksi === 0) {
            return response()->json([
                'success' => true,
                'message' => 'Belum ada data transaksi pada periode ini',
                'data'    => array_merge($ringkasanPeriode, [
                    'grafik_penjualan' => [],
                    'produk_terlaris'  => [],
                    'performa_kasir'   => [],
                ]),
            ]);
        }

        $grafikPenjualan = (clone $baseQuery)
            ->selectRaw('DATE(tanggal) as tanggal, SUM(total_bayar) as total')
            ->groupBy('tanggal')
            ->orderBy('tanggal')
            ->get();

        $produkTerlaris = DetailTransaksi::join('transaksis', 'transaksis.id_transaksi', '=', 'detail_transaksis.id_transaksi')
            ->where('transaksis.id_outlet', $idOutlet)
            ->where('transaksis.status', 'completed')
            ->whereBetween('transaksis.tanggal', [$mulai, $selesai])
            ->selectRaw('detail_transaksis.id_produk, SUM(detail_transaksis.jumlah) as total_terjual, SUM(detail_transaksis.subtotal) as total_omzet')
            ->groupBy('detail_transaksis.id_produk')
            ->orderByDesc('total_terjual')
            ->limit(10)
            ->with('produk:id_produk,nama_produk')
            ->get();

        $performaKasir = Transaksi::where('id_outlet', $idOutlet)
            ->where('status', 'completed')
            ->whereBetween('tanggal', [$mulai, $selesai])
            ->selectRaw('id_kasir, COUNT(*) as jumlah_transaksi, SUM(total_bayar) as total_penjualan')
            ->groupBy('id_kasir')
            ->orderByDesc('total_penjualan')
            ->with('kasir:id_user,nama')
            ->get();

        return response()->json([
            'success' => true,
            'data' => array_merge($ringkasanPeriode, [
                'grafik_penjualan' => $grafikPenjualan,
                'produk_terlaris'  => $produkTerlaris,
                'performa_kasir'   => $performaKasir,
            ]),
        ]);
    }
}