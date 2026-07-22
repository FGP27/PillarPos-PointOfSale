package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.model.UserEntity
import com.pilarkreasi.pillarpos.databinding.FragmentUsersBinding
import kotlinx.coroutines.launch

class UserFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: PillarPosDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        database = PillarPosDatabase.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.fabAddUser.setOnClickListener { showAddUserDialog() }
    }

    private fun setupRecyclerView() {
        database.userDao().getAllUsers().asLiveData().observe(viewLifecycleOwner) { users ->
            binding.rvUsers.adapter = UserAdapter(users) { user ->
                showDeleteConfirmation(user)
            }
        }
    }

    private fun showAddUserDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tambah Pegawai (Kasir)")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 20, 48, 20)

        val etUsername = EditText(requireContext()).apply { hint = "Username" }
        val etPassword = EditText(requireContext()).apply { 
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etUsername)
        layout.addView(etPassword)
        builder.setView(layout)

        builder.setPositiveButton("Simpan") { _, _ ->
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    database.userDao().insertUser(UserEntity(
                        idOutlet = 1, 
                        nama = username,
                        username = username,
                        password = password,
                        peran = Role.KASIR
                    ))
                    Toast.makeText(context, "Pegawai berhasil ditambah", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showDeleteConfirmation(user: UserEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pegawai")
            .setMessage("Yakin ingin menghapus ${user.username}?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    database.userDao().deleteUser(user.idUser)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
