<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\UserApi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi use case "Mengelola Data Pegawai" (SRS Fitur No. 10):
 * "Admin dapat menambah, mengubah, dan menonaktifkan akun kasir beserta
 * hak aksesnya." Seluruh endpoint di controller ini khusus Admin.
 */
class PegawaiController extends Controller
{
    /** Melihat daftar pegawai. */
    public function index(Request $request)
    {
        $query = UserApi::with('outlet');

        if ($request->filled('id_outlet')) {
            $query->where('id_outlet', $request->id_outlet);
        }

        if ($request->filled('peran')) {
            $query->where('peran', $request->peran);
        }

        $pegawai = $query->orderBy('nama')->get();

        return response()->json(['success' => true, 'data' => $pegawai]);
    }

    /** Melihat detail satu pegawai. */
    public function show(UserApi $pegawai)
    {
        return response()->json(['success' => true, 'data' => $pegawai->load('outlet')]);
    }

    /** Menambah pegawai baru (kasir atau admin). */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'id_outlet' => 'required|exists:outlets,id_outlet',
            'nama'      => 'required|string|max:150',
            'username'  => 'required|string|max:100|unique:user_apis,username',
            'password'  => 'required|string|min:6',
            'peran'     => 'required|in:admin,kasir',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $pegawai = UserApi::create([
            'id_outlet' => $request->id_outlet,
            'nama'      => $request->nama,
            'username'  => $request->username,
            'password'  => Hash::make($request->password),
            'peran'     => $request->peran,
            'status'    => true,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Pegawai berhasil ditambahkan',
            'data'    => $pegawai,
        ], 201);
    }

    /** Mengubah data pegawai (nama, username, outlet, peran, dan password opsional). */
    public function update(Request $request, UserApi $pegawai)
    {
        $validator = Validator::make($request->all(), [
            'id_outlet' => 'sometimes|required|exists:outlets,id_outlet',
            'nama'      => 'sometimes|required|string|max:150',
            'username'  => 'sometimes|required|string|max:100|unique:user_apis,username,' . $pegawai->id_user . ',id_user',
            'password'  => 'nullable|string|min:6',
            'peran'     => 'sometimes|required|in:admin,kasir',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        $data = $request->only(['id_outlet', 'nama', 'username', 'peran']);

        // Password cuma diubah kalau memang dikirim, supaya tidak ke-reset tanpa sengaja.
        if ($request->filled('password')) {
            $data['password'] = Hash::make($request->password);
        }

        $pegawai->update($data);

        return response()->json([
            'success' => true,
            'message' => 'Data pegawai berhasil diperbarui',
            'data'    => $pegawai,
        ]);
    }

    /**
     * Mengaktifkan/menonaktifkan akun pegawai (SRS Fitur No. 10: "menonaktifkan akun kasir").
     * Body: { "status": false }  -> nonaktifkan
     *       { "status": true }   -> aktifkan kembali
     */
    public function updateStatus(Request $request, UserApi $pegawai)
    {
        $validator = Validator::make($request->all(), [
            'status' => 'required|boolean',
        ]);

        if ($validator->fails()) {
            return response()->json(['success' => false, 'message' => $validator->errors()->first()], 422);
        }

        // Kalau dinonaktifkan, sekalian cabut semua token login yang aktif
        // supaya sesi yang sedang berjalan langsung ter-invalidasi.
        if (! $request->boolean('status')) {
            $pegawai->tokens()->delete();
        }

        $pegawai->update(['status' => $request->boolean('status')]);

        return response()->json([
            'success' => true,
            'message' => $request->boolean('status')
                ? 'Akun pegawai diaktifkan kembali'
                : 'Akun pegawai berhasil dinonaktifkan',
            'data' => $pegawai,
        ]);
    }
}