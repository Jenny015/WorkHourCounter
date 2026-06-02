package com.example.workhourcounter.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.workhourcounter.CardExpiryWorker
import com.example.workhourcounter.data.DatabaseHelper
import java.util.*
import java.util.concurrent.TimeUnit

data class CardModel(
    val id: Long = 0,         // Database unique primary key
    val name: String,         // E.g., "Health Insurance Card"
    val no: String,           // E.g., "ABC-12345"
    val expireDate: Long      // Expiration date stored as a millisecond timestamp
)

class CardsViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    val cardsList = mutableStateListOf<CardModel>()

    init { loadCards() }

    fun loadCards() {
        cardsList.clear()
        cardsList.addAll(dbHelper.getAllCards())
    }

    fun addCard(name: String, no: String, expireMs: Long) {
        val generatedId = dbHelper.insertCard(name, no, expireMs)
        scheduleExpiryNotification(generatedId, name, no, expireMs)
        loadCards()
    }

    fun updateCard(id: Long, name: String, no: String, expireMs: Long) {
        dbHelper.updateCard(id, name, no, expireMs)
        // Cancel previous pending notification worker and reschedule
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag("CARD_$id")
        scheduleExpiryNotification(id, name, no, expireMs)
        loadCards()
    }

    fun deleteCard(id: Long) {
        dbHelper.deleteCard(id)
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag("CARD_$id")
        loadCards()
    }

    private fun scheduleExpiryNotification(id: Long, name: String, no: String, expireMs: Long) {
        val calendar = Calendar.getInstance()
        val nowMs = calendar.timeInMillis

        // Target time calculation: Expiry date minus 30 days
        val targetNotifyMs = expireMs - (30L * 24 * 60 * 60 * 1000)
        val initialDelayMs = targetNotifyMs - nowMs

        if (initialDelayMs > 0) {
            val inputData = Data.Builder()
                .putString("CARD_NAME", name)
                .putString("CARD_NO", no)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<CardExpiryWorker>()
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("CARD_$id")
                .build()

            WorkManager.getInstance(getApplication()).enqueue(workRequest)
        }
    }
}