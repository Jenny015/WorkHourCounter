package com.example.workhourcounter

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Config {
    companion object {
        private const val PREFS_NAME = "app_settings"
        private lateinit var preferences: SharedPreferences

        // Call this once inside your MainActivity onCreate() before loading any UI
        fun init(context: Context) {
            preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun validateHourlyInput(input: String, input2: String = "0"): Boolean {
            if ((input.isEmpty() || input == ".") && (input2.isEmpty() || input2 == ".")) return true
            val floatValue = input.toFloatOrNull()
            val floatValue2 = input2.toFloatOrNull()
            return floatValue != null && floatValue2 != null && (floatValue + floatValue2) in 0.1f..24.0f
        }

        val MAX_TEXT_INPUT: Int = 20

        // Custom getters and setters pointing directly to persistent storage file mapping
        var fontSize: Int
            get() = preferences.getInt("fontSize", 1)
            set(value) = preferences.edit { putInt("fontSize", value) }

        var baseWorkHour: Float
            get() = preferences.getFloat("baseWorkHour", 8.0f)
            set(value) = preferences.edit { putFloat("baseWorkHour", value) }
    }
}