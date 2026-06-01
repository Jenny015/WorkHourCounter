package com.example.workhourcounter

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import java.util.Date

class WorkplaceViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    // This is a special reactive list. When it changes, the UI updates automatically!
    val workplaces = mutableStateListOf<Workplace>()

    init {
        loadWorkplaces()
    }

    // Load all data from SQLite into our reactive list
    fun loadWorkplaces() {
        workplaces.clear()
        workplaces.addAll(dbHelper.getAllWorkplaces())
    }

    // Add a new workplace to SQLite and refresh the list
    fun addWorkplace(name: String, status: String) {
        val newWorkplace = Workplace(
            name = name,
            startDate = System.currentTimeMillis(), // Today's date in milliseconds
            status = status
        )
        dbHelper.insertWorkplace(newWorkplace)
        loadWorkplaces() // Refresh UI list
    }
}