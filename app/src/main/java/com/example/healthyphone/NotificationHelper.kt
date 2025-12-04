package com.example.healthyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    companion object {
        private const val CHANNEL_ID_SERVICE = "HEALTHY_CHANNEL_ID"
        private const val CHANNEL_ID_ALERTS = "HEALTHY_ALERTS_ID"
        private const val NOTIFICATION_ID_SERVICE = 1
        private const val NOTIFICATION_ID_ALERT = 2
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID_SERVICE,
            "HealthyPhone Background Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val alertChannel = NotificationChannel(
            CHANNEL_ID_ALERTS,
            "HealthyPhone Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for bad posture and safety"
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }

    fun getBaseNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setContentTitle("HealthyPhone Monitor")
            .setContentText("Monitoring sensors...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    fun updatePersistentNotification(statusText: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setContentTitle("HealthyPhone Monitor")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SERVICE, notification)
    }

    fun sendHighPriorityAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun getNotificationId(): Int = NOTIFICATION_ID_SERVICE
}