package com.pilarkreasi.pillarpos.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pilarkreasi.pillarpos.databinding.ActivitySplashBinding
import com.pilarkreasi.pillarpos.ui.main.MainActivity
import com.pilarkreasi.pillarpos.ui.pilihperan.PilihPeranActivity
import com.pilarkreasi.pillarpos.util.SessionManager


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        binding.root.postDelayed({
            val intent = if (sessionManager.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, PilihPeranActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 800)
    }
}
