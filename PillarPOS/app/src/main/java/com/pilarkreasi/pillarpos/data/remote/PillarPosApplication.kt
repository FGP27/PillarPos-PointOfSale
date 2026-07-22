package com.pilarkreasi.pillarpos

import android.app.Application
import com.pilarkreasi.pillarpos.data.remote.RetrofitClient


class PillarPosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(applicationContext)
    }
}