package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentLaporanBinding
import java.text.NumberFormat
import java.util.Locale


class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!

    private val laporanViewModel: LaporanViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        LaporanViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPeriodeSpinner()
        observeData()
    }

    private fun setupPeriodeSpinner() {
        val opsi = listOf("Harian", "Mingguan", "Bulanan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opsi)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriode.adapter = adapter
        binding.spinnerPeriode.setSelection(0)

        binding.spinnerPeriode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val periode = when (position) {
                    1 -> Periode.MINGGUAN
                    2 -> Periode.BULANAN
                    else -> Periode.HARIAN
                }
                laporanViewModel.selectPeriode(periode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeData() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        laporanViewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalSales.text = formatter.format(summary?.totalSales ?: 0.0).replace("Rp", "Rp ")
            binding.tvTotalTransactions.text = "${summary?.totalTransactions ?: 0}"

            val average = if (summary != null && summary.totalTransactions > 0) {
                (summary.totalSales ?: 0.0) / summary.totalTransactions
            } else 0.0
            binding.tvAverage.text = formatter.format(average).replace("Rp", "Rp ")
        }

        laporanViewModel.dailyTrend.observe(viewLifecycleOwner) { trend ->
            val labels = trend.map { LaporanViewModel.formatLabel(it.day) }
            val values = trend.map { it.total }
            binding.chartSales.setData(labels, values)
        }

        laporanViewModel.topProducts.observe(viewLifecycleOwner) { products ->
            if (products.isNullOrEmpty()) {
                binding.rvTopProducts.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.rvTopProducts.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                binding.rvTopProducts.adapter = TopProductAdapter(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
