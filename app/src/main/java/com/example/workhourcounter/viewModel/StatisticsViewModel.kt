package com.example.workhourcounter.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.workhourcounter.data.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

data class WorkplaceStats(val name: String, val totalHours: Float)
data class MonthSummaryEntry(val monthLabel: String, val hours: Float)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    // Global High-Level States
    var totalDaysLogged by mutableIntStateOf(0)
    var lifetimeHours by mutableFloatStateOf(0f)

    val workplaceStatsList = mutableStateListOf<WorkplaceStats>()
    val monthlyTrendList = mutableStateListOf<MonthSummaryEntry>()

    fun generateStatistics() {
        val rawTripleData = dbHelper.getAllRecordsWithWorkplaceName()

        // Track global lifetime counters
        totalDaysLogged = rawTripleData.map { it.first.date }.distinct().size
        var combinedHours = 0f

        // Intermediate maps for grouping data
        val wpMap = mutableMapOf<String, Float>() // Name -> Pair(Hours, Earnings)
        val monthMap = mutableMapOf<String, Float>() // MonthLabel -> Pair(Hours, Earnings)

        val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (triple in rawTripleData) {
            val log = triple.first
            val wpName = triple.second

            val hoursForRecord = log.baseHours + log.otHours
            combinedHours += hoursForRecord

            // 1. Group by Workplace
            val currentWpData = wpMap.getOrDefault(wpName, 0f)
            wpMap[wpName] = currentWpData + hoursForRecord

            // 2. Group by Month Trend
            val monthLabel = monthFormatter.format(Date(log.date))
            val currentMonthData = monthMap.getOrDefault(monthLabel, 0f)
            monthMap[monthLabel] = currentMonthData + hoursForRecord
        }

        lifetimeHours = combinedHours

        // Format workplace list
        workplaceStatsList.clear()
        workplaceStatsList.addAll(wpMap.map { WorkplaceStats(it.key, it.value)})

        // Format and sort monthly trends (Descending order)
        monthlyTrendList.clear()
        monthlyTrendList.addAll(monthMap.map { MonthSummaryEntry(it.key, it.value)}.sortedByDescending { it.monthLabel })
    }
}