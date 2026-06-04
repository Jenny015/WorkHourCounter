package com.example.workhourcounter.data
import androidx.annotation.StringRes
import com.example.workhourcounter.R

data class Workplace(
    val id: Long = 0,
    val name: String,
    val startDate: Long,
    val status: String, // "WORKING", "PENDING", "FINISHED"
    val totalDays: Int = 0
)

data class WorkRecord(
    val id: Long = 0,
    val workplaceId: Long,
    val date: Long,
    val shiftType: String, // "FULL_DAY", "HALF_DAY", "CUSTOM"
    val baseHours: Float,
    val otHours: Float
)

enum class StatusOption(val dbValue: String, @param:StringRes val labelResId: Int) {
    WORKING("WORKING", R.string.wp_s_working),
    PENDING("PENDING", R.string.wp_s_pending),
    FINISHED("FINISHED", R.string.wp_s_finished);

    companion object {
        // String(DB value) -> StatusOption Object
        fun fromDbValue(value: String): StatusOption {
            return entries.find { it.dbValue == value } ?: WORKING
        }
    }
}

enum class ShiftTypeOption(val dbValue: String, @param:StringRes val labelResId: Int) {
    FULL_DAY("FULL_DAY", R.string.home_t_full),
    HALF_DAY("HALF_DAY", R.string.home_t_half),
    CUSTOM("CUSTOM", R.string.home_t_custom);

    companion object {
        // String(DB value) -> ShiftTypeOption Object
        fun fromDbValue(value: String): ShiftTypeOption {
            return entries.find { it.dbValue == value } ?: FULL_DAY
        }
    }
}