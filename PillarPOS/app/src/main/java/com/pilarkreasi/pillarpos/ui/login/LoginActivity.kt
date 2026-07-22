package com.pilarkreasi.pillarpos.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.repository.LoginResult
import com.pilarkreasi.pillarpos.databinding.ActivityLoginBinding
import com.pilarkreasi.pillarpos.ui.main.MainActivity
import com.pilarkreasi.pillarpos.util.SessionManager


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var peran: Role 
    private lateinit var sessionManager: SessionManager

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val peranName = intent.getStringExtra(EXTRA_PERAN) ?: Role.KASIR.name
        peran = Role.valueOf(peranName)

        binding.tvLoginTitle.text = if (peran == Role.ADMIN) {
            getString(R.string.login_admin_title)
        } else {
            getString(R.string.login_kasir_title)
        }

        binding.btnMasuk.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            binding.tvError.visibility = android.view.View.GONE
            viewModel.login(username, password, peran)
        }

        binding.btnGantiPeran.setOnClickListener {
            finish() 
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnMasuk.isEnabled = !loading
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    sessionManager.saveSession(
                        idUser = result.idUser,
                        nama = result.user.nama,
                        username = result.user.username,
                        peran = result.user.peran,
                        idOutlet = result.idOutlet,
                        authToken = result.authToken
                    )
                    Toast.makeText(this, R.string.msg_login_berhasil, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is LoginResult.FieldKosong -> tampilkanError(getString(R.string.msg_field_kosong))
                is LoginResult.KredensialSalah -> tampilkanError(getString(R.string.msg_login_salah))
                is LoginResult.TidakAdaKoneksi -> tampilkanError(getString(R.string.msg_tidak_ada_koneksi))
                else -> Unit
            }
        }
    }

    private fun tampilkanError(pesan: String) {
        binding.tvError.text = pesan
        binding.tvError.visibility = android.view.View.VISIBLE
    }

    companion object {
        const val EXTRA_PERAN = "extra_peran"
    }
}
