package com.example.workhourcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}
// TODO: fontSize & baseWorkHour -> shared preference
// TODO: Buttons content horizontal central alignment

// --- Advanced ---
// TODO: base hour for each workplace
// TODO: Location for each workplace -> click to open Google map
// TODO: Cards stores Images
