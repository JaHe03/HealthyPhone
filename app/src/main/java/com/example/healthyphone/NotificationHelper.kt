package com.example.healthyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val CHANNEL_ID = "HEALTHY_CHANNEL_ID"
    private val NOTIFICATION_ID = 1

    init {
        createNotificationChannel()
    }

    fun getBaseNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("HealthyPhone Monitor")
            .setContentText("Initializing sensors...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    fun updateNotification(
        isBadPosture: Boolean,
        isTooDark: Boolean,
        isWalking: Boolean,
        isScreenOn: Boolean
    ) {
        val text = when {
            isWalking && isScreenOn -> "Don't Text and Walk! Look Up."
            isWalking && !isScreenOn -> "running in background..."
            isBadPosture && isTooDark -> "Bad Posture & Too Dark!"
            isBadPosture -> "Bad Posture! Sit up!"
            isTooDark -> "It's too dark! Turn on a light."
            else -> "You are doing great."
        }

        val silentUpdate = text.contains("doing great") || text.contains("background")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("HealthyPhone Monitor")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOnlyAlertOnce(true)
            .setSilent(silentUpdate)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "HealthyPhone Background Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun getNotificationId(): Int = NOTIFICATION_ID
}