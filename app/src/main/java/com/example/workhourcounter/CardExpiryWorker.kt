package com.example.workhourcounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class CardExpiryWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val cardName = inputData.getString("CARD_NAME") ?: "Your Card"
        val cardNo = inputData.getString("CARD_NO") ?: ""

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "card_expiry_channel"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Card Expiring Soon!")
            .setContentText("$cardName ($cardNo) will expire in exactly 1 month.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
        return Result.success()
    }
}