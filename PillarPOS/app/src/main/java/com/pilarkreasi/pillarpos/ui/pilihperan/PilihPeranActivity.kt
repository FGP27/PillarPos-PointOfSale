package com.pilarkreasi.pillarpos.ui.pilihperan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.databinding.ActivityPilihPeranBinding
import com.pilarkreasi.pillarpos.ui.login.LoginActivity


class PilihPeranActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPilihPeranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMasukAdmin.setOnClickListener {
            bukaLogin(Role.ADMIN)
        }

        binding.btnMasukKasir.setOnClickListener {
            bukaLogin(Role.KASIR)
        }
    }

    private fun bukaLogin(peran: Role) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(LoginActivity.EXTRA_PERAN, peran.name)
        startActivity(intent)
    }
}
