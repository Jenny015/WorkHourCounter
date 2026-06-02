package com.example.workhourcounter.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.workhourcounter.data.DatabaseHelper
import com.example.workhourcounter.data.WorkRecord
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    var currentMonthHours by mutableStateOf(0f)
    var currentMonthSalary by mutableStateOf(0f)

    fun loadMonthSummary() {
        val calendar = Calendar.getInstance()

        // Target current month start boundaries
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startMs = calendar.timeInMillis

        // Target current month end boundaries
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 58)
        val endMs = calendar.timeInMillis

        val activeLogs = dbHelper.getRecordsInWindow(startMs, endMs)
        val ratesTimeline = dbHelper.getSalaryHistory()

        var totalHours = 0f
        var totalPay = 0f

        for (log in activeLogs) {
            val combinedHours = log.baseHours + log.otHours
            totalHours += combinedHours

            // Match log date against historical timeline entries
            val matchingSalary = ratesTimeline
                .filter { it.first <= log.date }
                .maxByOrNull { it.first }?.second ?: 0f

            // Assume standard 160-hour conversion calculation baseline framework context
            val hourlyEquivalentRate = matchingSalary / 160f

            // Assume 1.5x OT multiplier rule framework context
            val shiftValue = (log.baseHours * hourlyEquivalentRate) + (log.otHours * hourlyEquivalentRate * 1.5f)
            totalPay += shiftValue
        }

        currentMonthHours = totalHours
        currentMonthSalary = totalPay
    }

    fun logShift(workplaceId: Long, dateMs: Long, type: String, base: Float, ot: Float) {
        val record = WorkRecord(
            workplaceId = workplaceId,
            date = dateMs,
            shiftType = type,
            baseHours = base,
            otHours = ot
        )
        dbHelper.insertRecord(record)
        loadMonthSummary()
    }
}