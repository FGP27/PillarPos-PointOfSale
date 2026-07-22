package com.pilarkreasi.pillarpos.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID


object BluetoothPrinterHelper {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private const val PAPER_WIDTH_CHARS = 32

    sealed class PrintResult {
        object Success : PrintResult()
        object BluetoothNotSupported : PrintResult()
        object BluetoothDisabled : PrintResult()
        object PermissionDenied : PrintResult()
        object NoPairedDevice : PrintResult()
        data class ConnectionFailed(val message: String) : PrintResult()
    }

    fun hasConnectPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasScanPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    

    fun hasAllBluetoothPermissions(context: Context): Boolean {
        return hasConnectPermission(context) && hasScanPermission(context)
    }

    private fun getAdapter(context: Context): BluetoothAdapter? {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(context: Context): List<BluetoothDevice> {
        if (!hasConnectPermission(context)) return emptyList()
        val adapter = getAdapter(context) ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return adapter.bondedDevices?.toList() ?: emptyList()
    }

    fun checkAvailability(context: Context): PrintResult? {
        val adapter = getAdapter(context) ?: return PrintResult.BluetoothNotSupported
        if (!hasAllBluetoothPermissions(context)) return PrintResult.PermissionDenied
        if (!adapter.isEnabled) return PrintResult.BluetoothDisabled
        if (adapter.bondedDevices.isNullOrEmpty()) return PrintResult.NoPairedDevice
        return null
    }

    

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(
        context: Context,
        device: BluetoothDevice,
        storeName: String,
        address: String,
        trxId: String,
        tanggal: String,
        items: List<Triple<String, Int, Double>>,
        total: Double,
        paymentType: String
    ): PrintResult = withContext(Dispatchers.IO) {
        
        if (!hasAllBluetoothPermissions(context)) return@withContext PrintResult.PermissionDenied

        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            
            
            try {
                getAdapter(context)?.cancelDiscovery()
            } catch (e: SecurityException) {
                
            }
            socket.connect()

            val out = socket.outputStream
            out.write(buildReceiptBytes(storeName, address, trxId, tanggal, items, total, paymentType))
            out.flush()

            PrintResult.Success
        } catch (e: SecurityException) {
            PrintResult.PermissionDenied
        } catch (e: IOException) {
            PrintResult.ConnectionFailed(e.message ?: "Gagal terhubung ke printer")
        } finally {
            try {
                socket?.close()
            } catch (_: IOException) {
            }
        }
    }

    

    private const val ESC = 0x1B
    private const val GS = 0x1D

    private fun initPrinter() = byteArrayOf(ESC.toByte(), 0x40)
    private fun alignCenter() = byteArrayOf(ESC.toByte(), 0x61, 0x01)
    private fun alignLeft() = byteArrayOf(ESC.toByte(), 0x61, 0x00)
    private fun boldOn() = byteArrayOf(ESC.toByte(), 0x45, 0x01)
    private fun boldOff() = byteArrayOf(ESC.toByte(), 0x45, 0x00)
    private fun feedAndCut() = byteArrayOf(
        0x0A, 0x0A, 0x0A, 0x0A,
        GS.toByte(), 0x56, 0x42, 0x00
    )

    private fun buildReceiptBytes(
        storeName: String,
        address: String,
        trxId: String,
        tanggal: String,
        items: List<Triple<String, Int, Double>>,
        total: Double,
        paymentType: String
    ): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        fun text(s: String) = out.write(s.toByteArray(Charsets.UTF_8))
        fun line() = text("-".repeat(PAPER_WIDTH_CHARS) + "\n")

        out.write(initPrinter())
        out.write(alignCenter())
        out.write(boldOn())
        text(storeName.uppercase() + "\n")
        out.write(boldOff())
        if (address.isNotBlank()) text(address + "\n")
        text(tanggal + "\n")
        out.write(alignLeft())
        line()
        text("No. Transaksi: $trxId\n")
        text("Metode Bayar : $paymentType\n")
        line()

        items.forEach { (name, qty, price) ->
            text(wrapLine(name))
            val subtotal = qty * price
            val kiri = "  ${qty} x ${formatter.format(price).replace("Rp", "Rp")}"
            val kanan = formatter.format(subtotal).replace("Rp", "Rp")
            text(padTwoColumns(kiri, kanan) + "\n")
        }

        line()
        out.write(boldOn())
        text(padTwoColumns("TOTAL", formatter.format(total).replace("Rp", "Rp")) + "\n")
        out.write(boldOff())
        line()
        out.write(alignCenter())
        text("Terima kasih atas kunjungan Anda\n")
        text("Pillar POS\n")
        out.write(feedAndCut())

        return out.toByteArray()
    }

    private fun wrapLine(text: String): String {
        return if (text.length <= PAPER_WIDTH_CHARS) "$text\n" else "$text\n"
    }

    private fun padTwoColumns(left: String, right: String): String {
        val space = PAPER_WIDTH_CHARS - left.length - right.length
        return if (space > 0) left + " ".repeat(space) + right else "$left $right"
    }
}