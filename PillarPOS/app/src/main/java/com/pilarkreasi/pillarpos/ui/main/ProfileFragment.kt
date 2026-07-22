package com.pilarkreasi.pillarpos.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.databinding.FragmentProfileBinding
import com.pilarkreasi.pillarpos.ui.pilihperan.PilihPeranActivity
import com.pilarkreasi.pillarpos.util.SessionManager


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvProfileName.text = sessionManager.getNama() ?: "-"
        binding.tvProfileUsername.text = sessionManager.getUsername() ?: "-"

        val isAdmin = sessionManager.getRole() == Role.ADMIN
        binding.groupAdminMenu.visibility = if (isAdmin) View.VISIBLE else View.GONE

        binding.rowKelolaPegawai.setOnClickListener {
            openFullScreen(UserFragment())
        }

        binding.rowKelolaStok.setOnClickListener {
            openFullScreen(InventoryFragment())
        }

        binding.rowPersetujuanVoid.setOnClickListener {
            openFullScreen(VoidApprovalFragment())
        }

        binding.rowKelolaDiskon.setOnClickListener {
            openFullScreen(DiscountFragment())
        }

        binding.rowPengaturanOutlet.setOnClickListener {
            openFullScreen(OutletSettingsFragment())
        }

        binding.rowTentangAplikasi.setOnClickListener {
            showTentangAplikasi()
        }

        binding.rowLogout.setOnClickListener {
            confirmLogout()
        }
    }

    private fun openFullScreen(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(com.pilarkreasi.pillarpos.R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showTentangAplikasi() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Aplikasi")
            .setMessage(
                "Pillar POS\nVersi 1.0\n\nAplikasi kasir berbasis Android untuk usaha ritel " +
                        "dan F&B.\n\nPT. PILAR KREASI INOVASI"
            )
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Logout") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(requireContext(), PilihPeranActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}