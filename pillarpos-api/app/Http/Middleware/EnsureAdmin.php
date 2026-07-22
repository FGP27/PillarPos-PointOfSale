<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Membatasi endpoint hanya bisa diakses Admin/Pemilik Toko (SRS 2.3 Hak Akses:
 * Admin = Super Administrator, Kasir = Administrator Terbatas).
 */
class EnsureAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        if ($request->user()?->peran !== 'admin') {
            return response()->json([
                'success' => false,
                'message' => 'Hanya Admin yang boleh mengakses fitur ini',
            ], 403);
        }

        return $next($request);
    }
}