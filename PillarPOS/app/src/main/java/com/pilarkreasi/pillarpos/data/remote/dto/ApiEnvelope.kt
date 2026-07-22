package com.pilarkreasi.pillarpos.data.remote.dto


data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)