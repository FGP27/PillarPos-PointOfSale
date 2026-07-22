package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pilarkreasi.pillarpos.databinding.FragmentOutletSettingsBinding
import com.pilarkreasi.pillarpos.util.OutletSettingsManager


class OutletSettingsFragment : Fragment() {

    private var _binding: FragmentOutletSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var outletSettingsManager: OutletSettingsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutletSettingsBinding.inflate(inflater, container, false)
        outletSettingsManager = OutletSettingsManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etNamaOutlet.setText(outletSettingsManager.getNamaOutlet())
        binding.etAlamatOutlet.setText(outletSettingsManager.getAlamat())
        binding.etTeleponOutlet.setText(outletSettingsManager.getTelepon())

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnSaveOutlet.setOnClickListener { saveOutletSettings() }
    }

    private fun saveOutletSettings() {
        val nama = binding.etNamaOutlet.text.toString().trim()
        val alamat = binding.etAlamatOutlet.text.toString().trim()
        val telepon = binding.etTeleponOutlet.text.toString().trim()

        if (nama.isEmpty()) {
            Toast.makeText(context, "Nama outlet wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        outletSettingsManager.saveOutletSettings(nama, alamat, telepon)
        Toast.makeText(context, "Pengaturan outlet berhasil disimpan", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}