package com.example.workhourcounter.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.workhourcounter.data.DatabaseHelper
import com.example.workhourcounter.data.WorkRecord
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    var currentMonthHours by mutableStateOf(0f)

    val currentMonthRecords = mutableStateListOf<WorkRecord>()

    fun loadMonthSummary(calendarContext: Calendar) {
        val calendar = calendarContext.clone() as Calendar

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startMs = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
        val endMs = calendar.timeInMillis

        val activeLogs = dbHelper.getRecordsInWindow(startMs, endMs)
        //val ratesTimeline = dbHelper.getSalaryHistory()

        currentMonthRecords.clear()
        currentMonthRecords.addAll(activeLogs)

        var totalHours = 0f

        for (log in activeLogs) {
            totalHours += (log.baseHours + log.otHours)
        }

        currentMonthHours = totalHours
    }

    fun logShiftDirect(workplaceId: Long, dateMs: Long, type: String, base: Float, ot: Float, calendarContext: Calendar) {
        val record = WorkRecord(workplaceId = workplaceId, date = dateMs, shiftType = type, baseHours = base, otHours = ot)
        dbHelper.overrideOrInsertRecord(record)
        loadMonthSummary(calendarContext)
    }

    fun deleteRecord(recordId: Long, calendarContext: Calendar) {
        dbHelper.deleteRecordById(recordId)
        loadMonthSummary(calendarContext)
    }
}