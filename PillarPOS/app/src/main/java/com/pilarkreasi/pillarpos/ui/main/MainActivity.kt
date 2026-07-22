package com.pilarkreasi.pillarpos.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.repository.SyncRepository
import com.pilarkreasi.pillarpos.databinding.ActivityMainBinding
import com.pilarkreasi.pillarpos.ui.pilihperan.PilihPeranActivity
import com.pilarkreasi.pillarpos.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, PilihPeranActivity::class.java))
            finish()
            return
        }

        
        val role = sessionManager.getRole()
        val isAdmin = role == Role.ADMIN

        
        
        
        binding.bottomNav.menu.findItem(R.id.nav_activity).title =
            if (isAdmin) "Laporan" else "Transaksi"

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pos -> {
                    loadFragment(ProductFragment())
                    true
                }
                R.id.nav_home -> {
                    
                    
                    
                    loadFragment(if (isAdmin) DashboardFragment() else ProductFragment())
                    true
                }
                R.id.nav_activity -> {
                    
                    
                    loadFragment(if (isAdmin) LaporanFragment() else HistoryFragment())
                    true
                }
                R.id.nav_inventory -> {
                    
                    loadFragment(InventoryFragment())
                    true
                }
                R.id.nav_profil -> {
                    
                    
                    
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        
        if (savedInstanceState == null) {
            if (isAdmin) {
                loadFragment(DashboardFragment())
                binding.bottomNav.selectedItemId = R.id.nav_home
            } else {
                loadFragment(ProductFragment())
                binding.bottomNav.selectedItemId = R.id.nav_pos
            }
        }

        triggerSync()
    }

    override fun onResume() {
        super.onResume()
        triggerSync()
    }

    

    private fun triggerSync() {
        val syncRepository = SyncRepository.create(applicationContext)
        lifecycleScope.launch {
            syncRepository.syncPendingTransactions()
            syncRepository.pullMasterData()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}