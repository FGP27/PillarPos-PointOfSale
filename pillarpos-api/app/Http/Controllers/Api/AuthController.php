<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\UserApi;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

/**
 * Implementasi API untuk use case "Melakukan Login" (SRS Tabel 3.1).
 * Menangani skenario normal & alternatif (field kosong, kredensial salah).
 */
class AuthController extends Controller
{
    public function login(Request $request)
    {
        // Skenario Alternatif 8.a: "Username dan password wajib diisi"
        $validator = Validator::make($request->all(), [
            'username' => 'required|string',
            'password' => 'required|string',
            'peran'    => 'required|in:admin,kasir',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Username dan password wajib diisi',
            ], 422);
        }

        $user = UserApi::where('username', $request->username)
            ->where('peran', $request->peran)
            ->where('status', true)
            ->first();

        // Skenario Alternatif 8.b: "Username atau password salah"
        if (! $user || ! Hash::check($request->password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Username atau password salah',
            ], 401);
        }

        // Skenario Normal langkah 7-8: login berhasil
        $token = $user->createToken('pillarpos-mobile')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Login berhasil, selamat datang',
            'token'   => $token,
            'user'    => [
                'id_user'   => $user->id_user,
                'nama'      => $user->nama,
                'username'  => $user->username,
                'peran'     => $user->peran,
                'id_outlet' => $user->id_outlet,
            ],
        ]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Logout berhasil',
        ]);
    }

    public function me(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user(),
        ]);
    }
}