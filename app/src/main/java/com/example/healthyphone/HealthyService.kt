package com.example.healthyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class HealthyService : Service() {

    private lateinit var sensorHandler: SensorHandler

    override fun onCreate() {
        super.onCreate()
        sensorHandler = SensorHandler(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Create the Notification Channel (Required for Android 8+)
        createNotificationChannel()

        // 2. Build the Notification
        val notification = NotificationCompat.Builder(this, "HEALTHY_CHANNEL_ID")
            .setContentTitle("HealthyPhone is Active")
            .setContentText("Monitoring your posture and light levels...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, notification)

        sensorHandler.startListening()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "HEALTHY_CHANNEL_ID",
            "HealthyPhone Background Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}