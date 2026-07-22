package com.pilarkreasi.pillarpos.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentTransactionDetailBinding
import com.pilarkreasi.pillarpos.databinding.ItemReceiptRowBinding
import com.pilarkreasi.pillarpos.util.BluetoothPrinterHelper
import com.pilarkreasi.pillarpos.util.OutletSettingsManager
import com.pilarkreasi.pillarpos.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var outletSettingsManager: OutletSettingsManager

    private var transactionId: Int = -1
    private var currentTransaction: TransactionEntity? = null

    
    
    private lateinit var storeName: String
    private lateinit var storeAddress: String

    private val productViewModel: ProductViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        ProductViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
        )
    }

    
    
    private val bluetoothPermissionsNeeded: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            emptyArray()
        }

    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onCetakStrukClicked()
        } else {
            Toast.makeText(context, "Izin Bluetooth (Connect & Scan) diperlukan untuk mencetak struk", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        outletSettingsManager = OutletSettingsManager(requireContext())
        storeName = outletSettingsManager.getNamaOutlet()
        storeAddress = outletSettingsManager.getAlamat()
        transactionId = arguments?.getInt(ARG_TRANSACTION_ID) ?: -1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        productViewModel.observeTransaction(transactionId).observe(viewLifecycleOwner) { trx ->
            if (trx == null) return@observe
            currentTransaction = trx
            renderHeader(trx)
        }

        loadItems()

        binding.btnKirimWhatsapp.setOnClickListener { onKirimWhatsappClicked() }
        binding.btnCetakStruk.setOnClickListener { onCetakStrukClicked() }
        binding.btnAjukanVoid.setOnClickListener { showAjukanVoidDialog() }
    }

    private fun renderHeader(trx: TransactionEntity) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        binding.tvStoreName.text = storeName
        binding.tvTanggal.text = sdf.format(trx.transactionDate)

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotal.text = formatter.format(trx.totalAmount).replace("Rp", "Rp ")

        val isAdmin = sessionManager.getRole() == Role.ADMIN
        when (trx.paymentStatus) {
            "COMPLETED" -> {
                binding.tvStatusTitle.text = "Pembayaran Sukses"
                binding.tvVoidInfo.visibility = View.GONE
                binding.btnAjukanVoid.visibility = if (isAdmin) View.GONE else View.VISIBLE
            }
            "PENDING_VOID" -> {
                binding.tvStatusTitle.text = "Pengajuan Void"
                binding.tvVoidInfo.visibility = View.VISIBLE
                binding.tvVoidInfo.text = "Menunggu persetujuan Admin\nAlasan: ${trx.voidReason ?: "-"}"
                binding.btnAjukanVoid.visibility = View.GONE
            }
            else -> {
                binding.tvStatusTitle.text = "Transaksi Dibatalkan (Void)"
                binding.tvVoidInfo.visibility = View.VISIBLE
                binding.tvVoidInfo.text = "Alasan: ${trx.voidReason ?: "-"}"
                binding.btnAjukanVoid.visibility = View.GONE
            }
        }
    }

    private fun loadItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            val details = productViewModel.getDetailsWithProduct(transactionId)
            binding.layoutItems.removeAllViews()
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            details.forEachIndexed { index, item ->
                val row = ItemReceiptRowBinding.inflate(layoutInflater, binding.layoutItems, false)
                row.tvItemName.text = "${index + 1}. ${item.productName}"
                row.tvItemQty.text = "x${item.quantity}"
                row.tvItemSubtotal.text = formatter.format(item.quantity * item.priceAtTime).replace("Rp", "Rp ")
                binding.layoutItems.addView(row.root)
            }
        }
    }

    

    private fun onKirimWhatsappClicked() {
        val trx = currentTransaction ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val details = productViewModel.getDetailsWithProduct(transactionId)
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            val sb = StringBuilder()
            sb.append("*$storeName*\n")
            sb.append("${sdf.format(trx.transactionDate)}\n")
            sb.append("No. Transaksi: #TRX-${trx.idTransaction}\n\n")
            details.forEachIndexed { index, item ->
                sb.append("${index + 1}. ${item.productName} x${item.quantity} - ")
                sb.append(formatter.format(item.quantity * item.priceAtTime).replace("Rp", "Rp "))
                sb.append("\n")
            }
            sb.append("\n*Total: ${formatter.format(trx.totalAmount).replace("Rp", "Rp ")}*\n")
            sb.append("Metode Bayar: ${trx.paymentType}\n\n")
            sb.append("Terima kasih atas kunjungan Anda di $storeName")

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                setPackage("com.whatsapp")
            }
            try {
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                }
                startActivity(Intent.createChooser(fallback, "Kirim struk via"))
            }
        }
    }

    

    private fun hasAllBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return bluetoothPermissionsNeeded.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun onCetakStrukClicked() {
        
        if (!hasAllBluetoothPermissions()) {
            requestBluetoothPermissions.launch(bluetoothPermissionsNeeded)
            return
        }

        when (val issue = BluetoothPrinterHelper.checkAvailability(requireContext())) {
            BluetoothPrinterHelper.PrintResult.BluetoothNotSupported -> {
                Toast.makeText(context, "Perangkat ini tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
            }
            BluetoothPrinterHelper.PrintResult.BluetoothDisabled -> {
                Toast.makeText(context, "Aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
            BluetoothPrinterHelper.PrintResult.PermissionDenied -> {
                requestBluetoothPermissions.launch(bluetoothPermissionsNeeded)
            }
            BluetoothPrinterHelper.PrintResult.NoPairedDevice -> {
                Toast.makeText(
                    context,
                    "Belum ada printer yang di-pairing. Pairing dulu lewat Pengaturan Bluetooth HP.",
                    Toast.LENGTH_LONG
                ).show()
            }
            null -> showPrinterPickerAndPrint()
            else -> { 
 }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPrinterPickerAndPrint() {
        val devices = BluetoothPrinterHelper.getPairedDevices(requireContext())
        if (devices.size == 1) {
            printTo(devices[0])
            return
        }
        val names = devices.map { "${it.name ?: "Unknown"} (${it.address})" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Printer")
            .setItems(names) { _, which -> printTo(devices[which]) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun printTo(device: BluetoothDevice) {
        val trx = currentTransaction ?: return
        Toast.makeText(context, "Mencetak struk...", Toast.LENGTH_SHORT).show()

        viewLifecycleOwner.lifecycleScope.launch {
            val details = productViewModel.getDetailsWithProduct(transactionId)
            val items = details.map { Triple(it.productName, it.quantity, it.priceAtTime) }
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            val result = BluetoothPrinterHelper.printReceipt(
                context = requireContext(),
                device = device,
                storeName = storeName,
                address = storeAddress,
                trxId = "TRX-${trx.idTransaction}",
                tanggal = sdf.format(trx.transactionDate),
                items = items,
                total = trx.totalAmount,
                paymentType = trx.paymentType
            )

            when (result) {
                BluetoothPrinterHelper.PrintResult.Success ->
                    Toast.makeText(context, "Struk berhasil dicetak", Toast.LENGTH_SHORT).show()
                is BluetoothPrinterHelper.PrintResult.ConnectionFailed ->
                    Toast.makeText(context, "Gagal mencetak: ${result.message}", Toast.LENGTH_LONG).show()
                else ->
                    Toast.makeText(context, "Gagal mencetak struk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    

    private fun showAjukanVoidDialog() {
        val trx = currentTransaction ?: return
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ajukan Void Transaksi")
        builder.setMessage("Pengajuan ini akan menunggu persetujuan Admin sebelum transaksi benar-benar dibatalkan.")

        val input = EditText(requireContext())
        input.hint = "Alasan pembatalan"
        input.setPadding(48, 20, 48, 20)
        builder.setView(input)

        builder.setPositiveButton("Ajukan") { dialog, _ ->
            val reason = input.text.toString()
            if (reason.isNotEmpty()) {
                productViewModel.ajukanVoid(trx, reason)
                Toast.makeText(context, "Pengajuan void terkirim, menunggu persetujuan Admin", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Alasan wajib diisi", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"

        fun newInstance(transactionId: Int): TransactionDetailFragment {
            return TransactionDetailFragment().apply {
                arguments = Bundle().apply { putInt(ARG_TRANSACTION_ID, transactionId) }
            }
        }
    }
}