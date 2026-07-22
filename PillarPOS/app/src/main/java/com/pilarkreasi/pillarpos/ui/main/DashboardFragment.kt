package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var lastTotalSales: Double = 0.0
    private var lastTransactionCount: Int = 0

    private val productViewModel: ProductViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        ProductViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDashboard()
        setupMenuUtama()
    }

    private fun setupDashboard() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        productViewModel.todayTotalSales.observe(viewLifecycleOwner) { total ->
            lastTotalSales = total ?: 0.0
            binding.tvTotalSales.text = formatter.format(lastTotalSales).replace("Rp", "Rp ")
            updateAverage(formatter)
        }

        productViewModel.todayTransactionCount.observe(viewLifecycleOwner) { count ->
            lastTransactionCount = count
            binding.tvTransactionCount.text = "$count"
            updateAverage(formatter)
        }
    }

    private fun updateAverage(formatter: NumberFormat) {
        val average = if (lastTransactionCount > 0) lastTotalSales / lastTransactionCount else 0.0
        binding.tvAverage.text = formatter.format(average).replace("Rp", "Rp ")
    }

    

    private fun setupMenuUtama() {
        setupMenuItem(binding.menuTransaksi, android.R.drawable.ic_menu_recent_history, "Transaksi") {
            openFullScreen(HistoryFragment())
        }
        setupMenuItem(binding.menuProduk, android.R.drawable.ic_menu_agenda, "Produk") {
            openFullScreen(ProductFragment())
        }
        setupMenuItem(binding.menuLaporan, android.R.drawable.ic_menu_sort_by_size, "Laporan") {
            openFullScreen(LaporanFragment())
        }
        setupMenuItem(binding.menuPegawai, android.R.drawable.ic_menu_myplaces, "Pegawai") {
            openFullScreen(UserFragment())
        }
        setupMenuItem(binding.menuStok, android.R.drawable.ic_menu_manage, "Stok") {
            openFullScreen(InventoryFragment())
        }
        setupMenuItem(binding.menuVoid, android.R.drawable.ic_menu_close_clear_cancel, "Void") {
            openFullScreen(VoidApprovalFragment())
        }
    }

    private fun setupMenuItem(
        item: com.pilarkreasi.pillarpos.databinding.ItemDashboardMenuBinding,
        iconRes: Int,
        label: String,
        onClick: () -> Unit
    ) {
        item.menuIcon.setImageResource(iconRes)
        item.menuLabel.text = label
        item.menuRoot.setOnClickListener { onClick() }
    }

    private fun openFullScreen(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
