package com.example.workhourcounter.data

data class Workplace(
    val id: Long = 0,
    val name: String,
    val startDate: Long,
    val status: String, // "WORKING", "PENDING", "FINISHED"
    val totalDays: Int = 0 // ADD THIS LINE (default to 0)
)

data class WorkRecord(
    val id: Long = 0,
    val workplaceId: Long,
    val date: Long,
    val shiftType: String, // "FULL_DAY", "HALF_DAY", "CUSTOM"
    val baseHours: Float,
    val otHours: Float
)