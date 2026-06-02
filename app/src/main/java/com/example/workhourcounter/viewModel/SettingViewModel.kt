package com.example.workhourcounter.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.workhourcounter.data.DatabaseHelper

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    var paymentDay by mutableIntStateOf(7)
    val salaryHistory = mutableStateListOf<Pair<Long, Float>>()

    init {
        loadSettings()
    }

    fun loadSettings() {
        paymentDay = dbHelper.getPaymentDay()
        salaryHistory.clear()
        salaryHistory.addAll(dbHelper.getSalaryHistory())
    }

    fun updatePaymentDay(day: Int) {
        paymentDay = day
        dbHelper.savePaymentDay(day)
    }

    fun addSalaryRate(dateMs: Long, amount: Float) {
        dbHelper.insertOrUpdateSalary(dateMs, amount)
        loadSettings()
    }

    fun removeSalaryRate(dateMs: Long) {
        dbHelper.deleteSalaryRecord(dateMs)
        loadSettings()
    }
}