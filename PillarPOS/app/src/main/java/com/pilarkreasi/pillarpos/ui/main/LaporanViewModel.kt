package com.pilarkreasi.pillarpos.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Periode { HARIAN, MINGGUAN, BULANAN }

class LaporanViewModel(private val repository: ProductRepository) : ViewModel() {

    private val periode = MutableStateFlow(Periode.HARIAN)

    fun selectPeriode(p: Periode) {
        periode.value = p
    }

    fun periodeSaatIni(): Periode = periode.value

    

    private fun rangeFor(p: Periode): Pair<Long, Long> {
        val end = Calendar.getInstance()
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        val endMillis = end.timeInMillis + 1

        val start = Calendar.getInstance()
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        when (p) {
            Periode.HARIAN -> { 
 }
            Periode.MINGGUAN -> start.add(Calendar.DAY_OF_YEAR, -6) 
            Periode.BULANAN -> start.add(Calendar.DAY_OF_YEAR, -29) 
        }
        return start.timeInMillis to endMillis
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary = periode.flatMapLatest { p ->
        val (start, end) = rangeFor(p)
        repository.getSalesSummary(start, end)
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyTrend = periode.flatMapLatest { p ->
        val (start, end) = rangeFor(p)
        repository.getDailySalesTrend(start, end)
    }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val topProducts = periode.flatMapLatest { p ->
        val (start, end) = rangeFor(p)
        repository.getTopProducts(start, end)
    }.asLiveData()

    companion object {
        fun formatLabel(dayIso: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
                val display = SimpleDateFormat("dd/MM", Locale("id", "ID"))
                display.format(parser.parse(dayIso) ?: Date())
            } catch (e: Exception) {
                dayIso
            }
        }
    }
}

class LaporanViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LaporanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
